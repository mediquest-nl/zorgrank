(ns nl.mediquest.zorgrank-client.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 :slider-kwaliteit
 (fn [db]
   (:slider-kwaliteit db)))

(re-frame/reg-sub
 :slider-afstand
 (fn [db]
   (:slider-afstand db)))

(re-frame/reg-sub
 :slider-wachttijd
 (fn [db]
   (:slider-wachttijd db)))

(re-frame/reg-sub
 :filter-kwaliteit
 (fn [db]
   (:filter-kwaliteit db)))

(re-frame/reg-sub
 :filter-afstand
 (fn [db]
   (:filter-afstand db)))

(re-frame/reg-sub
 :filter-wachttijd
 (fn [db]
   (:filter-wachttijd db)))

(re-frame/reg-sub
 :zorgrank-response
 (fn [db]
   (:zorgrank-response db)))

(re-frame/reg-sub
 :postcode
 (fn [db]
   (:postcode db)))

(re-frame/reg-sub
 :specialisme-code
 (fn [db]
   (:specialisme-code db)))

(re-frame/reg-sub
 :specialist/selected
 (fn [db]
   (:specialist/selected db)))

(re-frame/reg-sub
 :specialist/confirmed
 (fn [db]
   (:specialist/confirmed db)))

(re-frame/reg-sub
 :auth/client-key
 (fn [db]
   (:auth/client-key db)))
