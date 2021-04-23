# Data
A data selection is made to reduce the amount of demo data. The demo data consists of a small percentage of our medical specialist care (medisch-specialistische zorg) data. The selected data was a snapshot of the data at a certain time, so the data is not up-to-date. The following choices have been made:

1. Only care centers are selected that are within the province of Utrecht.
    - The subset consists of 25 treatment locations (behandelcentra, behandellocaties)
1. Only the specialisms of Cardiologie (vektis_code 0320), Heelkunde (vektis_code 0303) and Orthopedie (vektis_code 0305) are selected.
    - In the table with diseases (aandoeningen) only diseases are selected that fall under one of these three specialisms.
1. Only the health insurance 'Zilveren Kruis' is selected, uzovi: 3311, 
1. Only valid patient postal codes (herkomst_pcs) between 3500 and 3700 are selected in the 'postal code distance table' (pc_reistijd).
   - These are postal codes (pcs) within the province of Utrecht.
