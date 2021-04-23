-- :name get-data :?
-- :doc Get data from zorgrank-datamart-aandoeningen
WITH pc AS (
  SELECT *
  FROM fct_pc4_reistijd_clean
  WHERE herkomst_pc = :pc4
),
check_aandoening_kwic_scores AS (
  -- Beide zijn gevuld of niet gevuld, dus check kan op 1
  SELECT count(1) AS aantal
  FROM zorgrank_datamart_aandoeningen zda
  WHERE icpc_1 = :icpc
   AND kwic_totaalscore_score IS NOT NULL
),
dataset_aandoening AS (
  SELECT zda.zorgaanbiederidentificatienummer AS "zorgaanbiederidentificatienummer",
    zd.bhc_name AS "organisatie-locatie",
    zd.codetype AS "zorgaanbiederidentificatienummer-codestelsel",
    zd.email AS "emailadres",
    zd.house_number AS "huisnummer",
    zd.house_number_addition AS "huisnummertoevoeging",
    zd.phone_number AS "telefoonnummer",
    zd.postal_code AS "postcode",
    zd.residence AS "woonplaats",
    zd.street AS "straat",
    zd.website,
    -- scores
    zd.hto_aantal AS "hto-aantal",
    ROUND(CAST (zd.quality_hto AS numeric), 1) AS "hto-score",
    zd.quality_hto_norm AS "hto-norm",
    CASE when zd.quality_hto IS NOT NULL
      THEN 'specialisme'
      ELSE NULL
    END AS "hto-type",
    zd.quality_patientervaring AS "patientervaring-score",
    zd.quality_patientervaring_norm AS "patientervaring-norm",
    zd.patientervaring_aantal AS "patientervaring-aantal",
    CASE when zd.quality_patientervaring IS NOT NULL
      THEN 'specialisme'
      ELSE NULL
    END AS "patientervaring-type",
    -- If at least one KWIC score exists for the aandoening, take kwic from aandoening for all.
    -- We use a custom type created by the cronjob to prevent having the same case when statement 4 times.
    CASE WHEN (SELECT aantal from check_aandoening_kwic_scores) > 0
      THEN (ROUND(CAST (zda.kwic_totaalscore_score AS numeric), 1), zda.kwic_sterscore_score,
        'aandoening', 'aandoening')::kwic_scores
      ELSE (ROUND(CAST (zd.quality_kwic_specialty AS numeric), 1), zd.quality_kwic_starscore,
        'specialisme', 'specialisme')::kwic_scores
    END AS kwic_scores,
    COALESCE(zda.wachttijd_toegangstijd_score, zd.wait_time) AS "wachttijd-toegangstijd-score",
    zda.wachttijd_behandeltijd_score AS "wachttijd-behandeltijd-score",
    wachttijd_behandeltijd_norm AS "wachttijd-behandeltijd-norm",
    CASE WHEN zda.wachttijd_toegangstijd_score IS NOT NULL
      THEN 'aandoening'
      WHEN zd.wait_time IS NOT NULL
      THEN 'specialisme'
      ELSE NULL
    END AS "wachttijd-toegangstijd-type",
    CASE WHEN zda.wachttijd_behandeltijd_score IS NOT NULL
      THEN 'aandoening'
      ELSE NULL
    END AS "wachttijd-behandeltijd-type",
    zd.specialty AS "specialisme",
    zd.specialisme_beschrijving AS "specialisme-code-naam",
    zd.specialisme_beschrijving_synoniem AS "specialisme-code-naam-synoniem",
    zda.icpc_1 AS "aandoening",
    zda.icpc_1_beschrijving AS "aandoening-code-naam",
    zda.icpc_1_beschrijving_synoniem AS "aandoening-code-naam-synoniem",
    -- Postcode
    pc.afstand_kortste_km AS "reisafstand",
    -- Zorgcontractering
    zc.contract_score AS "contract-score",
    CASE WHEN zc.contract_score IS NOT NULL
      THEN 'aandoening'
      ELSE NULL
    END AS "contract-type"
  FROM zorgrank_datamart_aandoeningen zda
  LEFT JOIN :i:datamart zd ON zda.zorgaanbiederidentificatienummer = zd.bhc_id
    AND zd.specialty = :specialty
  LEFT JOIN pc ON zd.pc_4 = pc.bestemmings_pc
  LEFT JOIN zorgcontractering_aandoeningen zc
    ON zda.zorgaanbiederidentificatienummer = zc.zorgaanbiederidentificatienummer
    AND zda.mq_aandoening_id = zc.mq_aandoening_id
    AND uzovi = :uzovi AND pakketcode = :pakketcode
  WHERE zda.icpc_1  = :icpc
  AND CASE WHEN EXISTS(
      SELECT 1
      FROM ref_icpc_mq_aandoening
      WHERE icpc1_code = :icpc
        AND ref_icpc_mq_aandoening.mq_koppeling_id = 2)
    THEN zda.specialisme = :specialty
    ELSE 1 = 1
    END
),
variables AS (
  SELECT MIN(reisafstand) AS reisafstand_min,
    percentile_disc(0.5) WITHIN GROUP (ORDER BY reisafstand) AS reisafstand_med,
    MAX((kwic_scores).kwic_totaal_score) AS kwic_totaalscore_max,
    percentile_disc(0.5) WITHIN GROUP (ORDER BY (kwic_scores).kwic_totaal_score)
    AS kwic_totaalscore_med,
    MIN("wachttijd-toegangstijd-score") AS wachttijd_toegangstijd_min,
    percentile_disc(0.5) WITHIN GROUP (ORDER BY "wachttijd-toegangstijd-score")
    AS wachttijd_toegangstijd_med
  FROM dataset_aandoening
), normalisation_calculations AS (
  -- greatest zet NULL naar 0 en min getallen ook naar 0.
  SELECT *,
    GREATEST(
      (1 - 0.5 * (reisafstand - (SELECT reisafstand_min FROM variables)) /
      CASE WHEN ((SELECT reisafstand_med FROM variables)
        - (SELECT reisafstand_min FROM variables)) = 0
        THEN (SELECT reisafstand_med FROM variables)
        ELSE ((SELECT reisafstand_med FROM variables)
        - (SELECT reisafstand_min FROM variables))
      END) * 100
    , 0) AS "reisafstand-norm",
    CASE WHEN ("hto-score" IS NULL AND (kwic_scores).kwic_ster_score IS NULL
      AND "patientervaring-score" IS NULL) THEN NULL
    ELSE
    (
      ( -- Numeric because dividing integers will result in 0.
        (COALESCE("hto-score", 1)-1)::numeric/(5-1) +
        (COALESCE((kwic_scores).kwic_ster_score, 1)-1)::numeric/(4-1) +
        (COALESCE("patientervaring-score", 1)-1)::numeric/(10-1)
      )
      /
      (
        CASE WHEN "hto-score" IS NULL THEN 0 ELSE 1 END +
        CASE WHEN (kwic_scores).kwic_ster_score IS NULL THEN 0 ELSE 1 END +
        CASE WHEN "patientervaring-score" IS NULL THEN 0 ELSE 1 END
      ) * 100
    ) END AS mq_kwaliteit_score,
    (1 + 0.5 * ((kwic_scores).kwic_totaal_score - (SELECT kwic_totaalscore_max FROM variables)) /
    CASE WHEN ((SELECT kwic_totaalscore_max FROM variables) -
        (SELECT kwic_totaalscore_med FROM variables)) = 0
      THEN (SELECT kwic_totaalscore_med FROM variables)
      ELSE ((SELECT kwic_totaalscore_max FROM variables) -
        (SELECT kwic_totaalscore_med FROM variables))
    END) * 100 AS kwic_totaalscore_norm,
    (1 - 0.5 * ("wachttijd-toegangstijd-score" -
    (SELECT wachttijd_toegangstijd_min FROM variables)) /
    CASE WHEN ((SELECT wachttijd_toegangstijd_med FROM variables) -
        (SELECT wachttijd_toegangstijd_min FROM variables)) = 0
      THEN (SELECT wachttijd_toegangstijd_med FROM variables)
      ELSE ((SELECT wachttijd_toegangstijd_med FROM variables)-
        (SELECT wachttijd_toegangstijd_min FROM variables))
    END) * 100 AS wachttijd_toegangstijd_norm
  FROM dataset_aandoening
)
SELECT
  zorgaanbiederidentificatienummer,
  "organisatie-locatie",
  "zorgaanbiederidentificatienummer-codestelsel",
  emailadres,
  huisnummer,
  huisnummertoevoeging,
  telefoonnummer,
  postcode,
  woonplaats,
  straat,
  website,
  specialisme,
  "specialisme-code-naam",
  "specialisme-code-naam-synoniem",
  aandoening,
  "aandoening-code-naam",
  "aandoening-code-naam-synoniem",
  "hto-aantal",
  "hto-score",
  "hto-type",
  "patientervaring-aantal",
  -- ROUND does not work on doubles!
  ROUND("patientervaring-score"::numeric, 1) AS "patientervaring-score",
  "patientervaring-norm",
  "patientervaring-type",
  (kwic_scores).kwic_totaal_score AS "kwic-totaalscore-score",
  (kwic_scores).kwic_ster_score AS "kwic-sterscore-score",
  (kwic_scores).kwic_totaalscore_type AS "kwic-totaalscore-type",
  (kwic_scores).kwic_sterscore_type "kwic-sterscore-type",
  "wachttijd-toegangstijd-score",
  "wachttijd-behandeltijd-score",
  CASE WHEN wachttijd_toegangstijd_norm IS NULL THEN 0
    WHEN wachttijd_toegangstijd_norm < 0 THEN 0
    WHEN wachttijd_toegangstijd_norm > 100 THEN 100
    ELSE wachttijd_toegangstijd_norm
  END AS "wachttijd-toegangstijd-norm",
  "wachttijd-behandeltijd-norm",
  "wachttijd-toegangstijd-type",
  "wachttijd-behandeltijd-type",
  mq_kwaliteit_score AS "mq-kwaliteit-score",
  (COALESCE("hto-norm", 0) +
  COALESCE(
    CASE WHEN kwic_totaalscore_norm < 0 THEN 0
         WHEN kwic_totaalscore_norm > 100 THEN 100
         ELSE kwic_totaalscore_norm
    END,
    0) +
  COALESCE("patientervaring-norm", 0)
  )
  /
  GREATEST((
    CASE WHEN "hto-norm" IS NULL THEN 0 ELSE 1 END +
    CASE WHEN  kwic_totaalscore_norm IS NULL THEN 0 ELSE 1 END +
    CASE WHEN "patientervaring-norm" IS NULL THEN 0 ELSE 1 END
    ),1
  ) AS "mq-kwaliteit-norm",
  reisafstand,
  "reisafstand-norm",
  "contract-score",
  "contract-type"
FROM normalisation_calculations
