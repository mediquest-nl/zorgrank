(ns nl.mediquest.zorgrank-client.component.footer)

(defn init []
  [:footer.footer
   [:div.container
    [:div.columns
     [:div.column.is-4.has-text-centered.is-hidden-tablet
      [:a.title.is-4.has-text-white {:href "#"} "Mediquest"]]
     [:div.column.is-4
      [:div.level
       [:a.level-item {:href "#"} ""]
       [:a.level-item {:href "#"} ""]]]
     [:div.column.is-4.has-text-centered.is-hidden-mobile
      [:a.title.is-4.has-text-white {:href "#"} "Mediquest"]]
     [:div.column.is-4.has-text-right
      [:div.level
       [:a.level-item {:href "#"} ""]
       [:a.level-item {:href "#"} ""]]]]
    [:p.subtitle.has-text-centered.is-6.has-text-white
     "© 2019-2021 Mediquest. All right reserved."]]])
