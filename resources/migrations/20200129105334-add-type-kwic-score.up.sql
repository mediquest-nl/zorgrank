DROP TYPE IF EXISTS kwic_scores;
--;;
CREATE TYPE kwic_scores AS (kwic_totaal_score numeric, kwic_ster_score int, kwic_totaalscore_type text, kwic_sterscore_type text);
