(ns nl.mediquest.zorgrank-client.page.home
  (:require
   [adzerk.env :as env]
   [nl.mediquest.zorgrank-client.component.dropdown.core :as component.dropdown]
   [nl.mediquest.zorgrank-client.data.icpcs :as data.icpcs]
   [nl.mediquest.zorgrank-client.subs]
   [nl.mediquest.zorgrank-client.utils :refer [sort-map-by-value]]
   [re-frame.core :as re-frame]))

(defn component-disclaimer []
  [:div.has-text-left
   [:h1.is-size-3 "Disclaimer ZorgRank"]
   [:p
    "Door ZorgRank te gebruiken, verklaart u bekend te zijn met de inhoud van de
     disclaimer en akkoord te zijn met de toepasselijkheid daarvan."]

   [:p
    "ZorgRank is een algoritme (rekenkundig model) dat een persoonlijk advies
     geeft voor de best passende aanbieder van medisch specialistische vervolgzorg
     in Nederland. Met dit advies kunnen zorgvrager en verwijzer vervolgens
     onderbouwd samen beslissen."]

   [:p
    "De informatie wat betreft ZorgRank is met de grootste zorgvuldigheid
     samengesteld. Het gebruik van ZorgRank geschiedt volledig op basis van uw
     eigen verantwoordelijkheid. ZorgRank biedt ondersteuning aan de zorgvrager en
     de verwijzer om tot een keuze te komen voor de best passende zorgaanbieder.
     Deze beslissing is uitsluitend de verantwoordelijkheid van de zorgvrager in
     afstemming met de verwijzer."]

   [:p
    "ZIO/ TIPP en Mediquest zijn niet aansprakelijk uit enige rechtsgrond,
     indien de informatie en/of het gebruik van ZorgRank niet volledig voldoet aan
     juistheid, actualiteit, volledigheid of effectiviteit"]])

(def sorting-items
  [{:value nil :content "Geen Sortering"}
   {:value :wachttijd :content "Wachttijd"}
   {:value :afstand :content "Afstand"}
   {:value :kwaliteit :content "Kwaliteit"}])

(def specialisten
  {"0303" "Chirurgie"
   "0305" "Orthopedie"
   "0320" "Cardiologie"})

(def icpcs
  (->> data.icpcs/data
       (map (fn [[value content]] {:value value :content content}))
       (sort-by :content)
       (cons {:value nil :content "Geen aandoening"})))

(env/def MEDIQUEST_KEY
  "00000000-0000-0000-0000-000000000000")

(def clients
  (into {} [[MEDIQUEST_KEY "Mediquest"]]))

(defn verify-specialisme-code
  [specialisme-code]
  (when-not (empty? specialisme-code)
    specialisme-code))

(defn verify-client-key
  [client-key]
  (when-not (empty? client-key)
    client-key))

(defn on-change-slider-fn
  [slider-id object]
  (re-frame/dispatch [:slider-event slider-id (-> object .-target .-value)]))

(defn on-change-specialist-fn
  [object]
  (let [specialisme-code (-> object .-target .-value verify-specialisme-code)]
    (re-frame/dispatch [:set-specialisme-code specialisme-code])))

(defn on-change-client-fn
  [object]
  (let [client-key (-> object .-target .-value verify-client-key)]
    (re-frame/dispatch [:auth/set-client-key client-key])))

(defn on-change-postcode-fn
  [object]
  (re-frame/dispatch [:set-postcode (-> object .-target .-value)]))

(defn on-change-filter-fn
  [filter-id object]
  (re-frame/dispatch [:set-filter filter-id (-> object .-target .-value)]))

(def no-value "Onbekend")

(defn format-kwaliteit
  [kwaliteit]
  (if kwaliteit
    (.toFixed kwaliteit 1)
    no-value))

(defn get-afstand
  [specialist]
  (if-let [afstand (:reisafstand specialist)]
    (str afstand " KM")
    no-value))

(defn get-wachttijd
  [specialist]
  (if-let [wachttijd (-> specialist :wachttijd_toegangstijd :score)]
    (str wachttijd " week")
    no-value))

(defn get-hto-totaalindruk
  [specialist]
  (format-kwaliteit (-> specialist :hto :score)))

(defn get-kwic-totaalscore
  [specialist]
  (format-kwaliteit (-> specialist :kwic_totaalscore :score)))

(defn get-score
  [specialist]
  (format-kwaliteit (:score specialist)))

(defn get-kwaliteit-genormaliseerd
  [specialist]
  (format-kwaliteit (:mq_kwaliteit_genormaliseerd specialist)))

(defn component-slider [slider-id name]
  (let [slider-state (re-frame/subscribe [slider-id])]
    [:div
     [:label (str @slider-state " " name)]
     [:input.range-slider
      {:type "range" :min "1" :max "100" :value @slider-state
       :on-mouse-up #(re-frame/dispatch [:set-mouse-down false])
       :on-mouse-down #(re-frame/dispatch [:set-mouse-down true])
       :on-change (partial on-change-slider-fn slider-id)}]]))

(defn placeholder-option-props [placeholder-name opts]
  {:key placeholder-name
   :value (when-not (:disabled opts true) "")
   :disabled (:disabled opts true)
   :hidden (:hidden opts true)})

(defn placeholder-option
  ([placeholder-name]
   (placeholder-option placeholder-name {}))
  ([placeholder-name opts]
   [:option
    (placeholder-option-props placeholder-name opts)
    placeholder-name]))

(defn add-placeholder-option
  ([options placeholder-name]
   (add-placeholder-option options placeholder-name {}))
  ([options placeholder-name opts]
   (cons (placeholder-option placeholder-name opts)
         options)))

(defn make-select-options [options]
  (doall
   (for [[option-key option-value] options]
     ^{:key option-key}
     [:option {:value option-key} option-value])))

(def sort-specialists-by-name
  sort-map-by-value)

(def sort-clients-by-name
  sort-map-by-value)

(defn component-specialist-selector []
  (let [placeholder "Specialisme"
        specialisme-code (re-frame/subscribe [:specialisme-code])]
    [:div.select
     [:select.select
      {:on-change on-change-specialist-fn
       :value (or @specialisme-code "Specialisme")}
      (doall
       (-> specialisten
           sort-specialists-by-name
           make-select-options
           (add-placeholder-option placeholder)))]]))

(defn component-icpc-selector []
  [component.dropdown/init
   {:dropdown-name "ICPC"
    :on-submit #(re-frame/dispatch [:api/zorgrank-request])
    :show-search? true
    :title "ICPC"
    :items icpcs}])

(defn component-sorting []
  [component.dropdown/init
   {:dropdown-name "Sortering"
    :on-submit #(re-frame/dispatch [:api/zorgrank-request])
    :title "Sortering"
    :items sorting-items}])

(defn component-filter [placeholder filter-id]
  (let [filter-value (re-frame/subscribe [filter-id])]
    [:input.input.is-primary
     {:placeholder placeholder
      :maxLength "3"
      :on-change (partial on-change-filter-fn filter-id)
      :value @filter-value}]))

(defn component-filters []
  [:div
   [:h3.has-margin-top-5.has-margin-bottom-5 "Filters"]
   [component-filter "Min kwaliteit" :filter-kwaliteit]
   [component-filter "Max afstand" :filter-afstand]
   [component-filter "Max wachttijd" :filter-wachttijd]])

(defn component-postcode []
  (let [postcode (re-frame/subscribe [:postcode])]
    [:input.input.is-primary
     {:placeholder "Postcode"
      :maxLength 6
      :value @postcode
      :on-change on-change-postcode-fn}]))

(defn view-column
  ([title content]
   (view-column {} title content))
  ([attr title content]
   [:div.column
    attr
    [:b (str title ": ")]
    content]))

(defn zorgcontractering-type [zorgcontractering]
  (str "Type: " (:type zorgcontractering)
       "\n"
       "Beschrijving: " (:code_naam_synoniem zorgcontractering)
       "\n"))

(defn zorgcontractering
  [specialist]
  (when-let [zorgcontractering (:zorgcontractering specialist)]
    [:div.column
     [:div {:data-tooltip
            (str (zorgcontractering-type zorgcontractering)
                 (:score_beschrijving zorgcontractering))}
      [:b "Zorgcontractering: "]
      (:score_titel zorgcontractering)]]))

(defn get-address
  [specialist]
  (str
   (:straat specialist) " "
   (:huisnummer specialist)
   (:huisnummertoevoeging specialist)))

(defn modal-title
  [title]
  [:div.columns
   [:div.column
    [:h2.title title]]])

(defn modal-header
  [specialist]
  [:div.card-header
   [:p.card-header-title (:organisatie_locatie specialist)]
   [:b.card-header-icon (:rank specialist)]])

(defn score-tooltip-text [score]
  (str
   "Type: " (:type score)
   "\n"
   "Beschrijving: " (:code_naam_synoniem score)
   "\n"
   "Code: " (:code score)))

(defn score-tooltip [score]
  (when score
    {:data-tooltip (score-tooltip-text score)}))

(defn modal-content
  [specialist]
  [:div.card-content
   [:div.content
    [modal-title "Info"]
    [:div.columns
     [view-column "Adres" (get-address specialist)]
     [view-column "Plaats" (:woonplaats specialist)]]
    [:div.columns
     [view-column "Website" (:website specialist)]
     [view-column "Telefoonnummer" (:telefoonnummer specialist)]]
    [modal-title "Scores"]
    [:div.columns
     [view-column "Afstand" (get-afstand specialist)]
     [view-column
      (score-tooltip (:wachttijd_toegangstijd specialist))
      "Wachttijd"
      (get-wachttijd specialist)]]
    [:div.columns
     [view-column
      (score-tooltip (:hto specialist))
      "HTO totaalindruk" (get-hto-totaalindruk specialist)]
     [view-column
      (score-tooltip (:kwic_totaalscore specialist))
      "KWIC Score" (get-kwic-totaalscore specialist)]]]])

(defn modal-footer
  [specialist]
  [:div.card-footer
   [:a.card-footer-item.has-text-danger.is-size-6.has-text-weight-semibold
    {:on-click #(re-frame/dispatch [:specialist/choose nil])}
    "Annuleren"]
   [:a.card-footer-item.has-text-info.is-size-6.has-text-weight-semibold
    {:on-click (fn []
                 (re-frame/dispatch [:specialist/confirm specialist])
                 (re-frame/dispatch [:specialist/choose nil]))}
    "Bevestigen"]])

(defn choose-modal []
  (let [specialist (re-frame/subscribe [:specialist/selected])]
    [:div.modal {:class (if @specialist "is-active" "is-disabled")}
     [:div.modal-background
      {:on-click #(re-frame/dispatch [:specialist/choose nil])}]
     [:div.modal-content
      [:div.card.specialist-card
       [modal-header @specialist]
       [modal-content @specialist]
       [modal-footer @specialist]]]
     [:button.modal-close.is-large
      {:on-click #(re-frame/dispatch [:specialist/choose nil])}]]))

(defn modals []
  [choose-modal])

(defn component-notification []
  (let [specialist (re-frame/subscribe [:specialist/confirmed])]
    (when @specialist
      [:div.notification.is-success
       [:button.delete
        {:on-click #(re-frame/dispatch [:specialist/confirm nil])}]
       (str "Bedankt dat u heeft gekozen voor " (:organisatie_locatie @specialist))])))

(defn specialist-row [specialist]
  [:div.column.is-6
   [:div.card.specialist-card
    [:div.card-header
     [:p.card-header-title (:organisatie_locatie specialist)]
     [:b.card-header-icon (:rank specialist)]]
    [:div.card-content.has-text-left
     [:div.content
      [view-column "Afstand" (get-afstand specialist)]
      [view-column
       (score-tooltip (:wachttijd_toegangstijd specialist))
       "Wachttijd" (get-wachttijd specialist)]
      [view-column
       (score-tooltip (:hto specialist))
       "HTO totaalindruk"
       (get-hto-totaalindruk specialist)]
      [view-column
       (score-tooltip (:kwic_totaalscore specialist))
       "KWIC Score"
       (get-kwic-totaalscore specialist)]
      (let [patientervaring (:patientervaring specialist)]
        [view-column
         (score-tooltip patientervaring)
         "Patientervaring" (or (:score patientervaring) "Onbekend")])
      [view-column "Algoritme Score" (get-score specialist)]
      [zorgcontractering specialist]]]
    [:div.card-footer
     [:a.card-footer-item
      {:on-click #(re-frame/dispatch [:specialist/choose specialist])}
      "Selecteer"]]]])

(defn specialists-view []
  (let [zorgrank-response (re-frame/subscribe [:zorgrank-response])]
    [:div.column.is-8.columns.is-multiline.has-text-centered
     (doall
      (for [{:keys [organisatie_locatie] :as specialist} @zorgrank-response]
        ^{:key organisatie_locatie}
        [specialist-row specialist]))]))

(defn filter-view []
  [:div.column.is-4
   [:div.button.has-margin-bottom-3
    {:on-click #(re-frame/dispatch [:standaard-waarden])}
    "Standaard waarden"]
   [component-postcode]
   [component-specialist-selector]
   [component-icpc-selector]
   [:h3.has-margin-top-5 "Voorkeuren"]
   [component-sorting]
   [:h4.has-margin-top-5.has-margin-bottom.is-size-5 "Wegingsfactor"]
   [component-slider :slider-kwaliteit "Kwaliteit (-/+)"]
   [component-slider :slider-afstand "Afstand (-/+)"]
   [component-slider :slider-wachttijd "Wachttijd (-/+)"]
   [component-filters]])

(defn init []
  [:section.section
   [:div.container
    [component-notification]
    [modals]
    [:div.columns
     [filter-view]
     [specialists-view]]
    [component-disclaimer]]])
