## External API

By default the development client targets your local zorgrank-api. To target a
different API (for example the online demo API) you can set the environment variable
"ZORGRANK_URI".

    ZORGRANK_URI=https://zorgrank-demo.mediquest.cloud/api/v3/zorgrank-request npx shadow-cljs watch dev
