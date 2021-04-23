-- :name get-data :?
-- :doc Get data from zorgrank-datamart
WITH zc AS (
  SELECT contract_score, zorgaanbiederidentificatienummer
  FROM zorgcontractering_specialisme
  WHERE uzovi = :uzovi AND pakketcode = :pakketcode AND specialisme = :specialty),
pc AS (
  SELECT *
  FROM fct_pc4_reistijd_clean
  WHERE herkomst_pc = :pc4
),
dataset AS (
  SELECT zd.bhc_id AS "zorgaanbiederidentificatienummer",
    bhc_name AS "organisatie-locatie",
    codetype AS "zorgaanbiederidentificatienummer-codestelsel",
    email AS "emailadres",
    house_number AS "huisnummer",
    house_number_addition AS "huisnummertoevoeging",
    phone_number AS "telefoonnummer",
    postal_code AS "postcode",
    hto_aantal AS "hto-aantal",
    patientervaring_aantal AS "patientervaring-aantal",
    ROUND(CAST (quality_hto AS numeric), 1) AS "hto-score",
    ROUND(CAST (quality_kwic_specialty AS numeric), 1) AS "kwic-totaalscore-score",
    ROUND(CAST (quality_patientervaring AS numeric), 1) AS "patientervaring-score",
    ROUND(CAST (quality_norm AS numeric), 1) AS "mq-kwaliteit-norm",
    ROUND(CAST (quality_score AS numeric), 1) AS "mq-kwaliteit-score",
    quality_kwic_starscore AS "kwic-sterscore-score",
    residence AS "woonplaats",
    street AS "straat",
    wait_time AS "wachttijd-toegangstijd-score",
    wait_time_norm AS "wachttijd-toegangstijd-norm",
    NULL AS "wachttijd-behandeltijd-score",
    NULL AS "wachttijd-behandeltijd-norm",
    website AS "website",
    pc.afstand_kortste_km AS "reisafstand",
    zc.contract_score AS "contract-score",
    'specialisme' AS "contract-type",
    'specialisme' AS "hto-type",
    'specialisme' AS "kwic-totaalscore-type",
    'specialisme' AS "kwic-sterscore-type",
    'specialisme' AS "patientervaring-type",
    'specialisme' AS "wachttijd-toegangstijd-type",
    'specialisme' AS "wachttijd-behandeltijd-type",
    zd.specialty AS "specialisme",
    specialisme_beschrijving AS "specialisme-code-naam",
    specialisme_beschrijving_synoniem AS "specialisme-code-naam-synoniem"
    FROM :i:datamart zd
  LEFT JOIN pc ON zd.pc_4 = pc.bestemmings_pc
  LEFT JOIN zc ON zd.bhc_id = zc.zorgaanbiederidentificatienummer
  WHERE zd.specialty = :specialty
    -- Blacklisted items with a whitelist aandoening should only be visible in combination with an aandoening.‌
    AND COALESCE(zd.exclusie_op_niveau_specialisme, FALSE) <> True
),
variables AS (
  SELECT MIN(reisafstand) AS reisafstand_min,
    percentile_disc(0.5) within GROUP (ORDER BY reisafstand) AS reisafstand_med
  FROM dataset
)
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
      , 0) AS "reisafstand-norm"
FROM dataset
