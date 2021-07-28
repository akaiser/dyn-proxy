## DynProxy

Simple http proxy server/impl capable of forwarding requests to dynamic destinations.

This example is embedding a Tomcat to expose a proxy servlet. Can be embedded into Java based webapps to bypass CORS or used as a standalone service to bridge port access limitations for other backend services.  

### Usage & Examples
```
curl -i 'dyn-proxy.herokuapp.com/proxy?_host=api.tvmaze.com:443&_path=search/shows&q=scrubs' -H 'Accept: application/json'
curl -i 'dyn-proxy.herokuapp.com/proxy?_host=dashboard.nbshare.io:443&_path=api/v1/apps/reddit' -H 'Accept: application/json'
curl -i 'dyn-proxy.herokuapp.com/proxy?_host=date.nager.at:443&_path=api/v3/PublicHolidays/2021/GB' -H 'Accept: application/json'
curl -i 'dyn-proxy.herokuapp.com/proxy?_host=api.coinlore.net:443&_path=api/tickers/&start=0&limit=10' -H 'Accept: application/json'
curl -i 'dyn-proxy.herokuapp.com/proxy?_host=api.openweathermap.org:443&_path=data/2.5/weather&zip=95050' -H 'Accept: application/json'
```
