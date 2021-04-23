(ns nl.mediquest.zorgrank-client.component.header)

(defn init []
  [:nav.navbar.has-shadow
   [:div.container
    [:div.navbar-brand
     [:a.navbar-item {:href "/"}
      [:img
       {:width "150"
        :alt "Mediquest"
        :src "/images/logo-mediquest_rgb.jpg"}]]
     [:div.is-divider-vertical.is-hidden-mobile]
     [:div.title.is-4.has-margin-3.has-text-primary.is-hidden-mobile
      "Gepast verwijzen"]
     [:div.has-margin-left-auto.has-items-center
      [:a.navbar-burger
       [:span]
       [:span]
       [:span]]]]
    [:div.navbar-menu
     [:div.navbar-start
      [:a.navbar-item {:href "#"} "API 1.0"]
      [:a.navbar-item {:href "#"} "API 2.0"]]
     [:div.navbar-end]]]])
