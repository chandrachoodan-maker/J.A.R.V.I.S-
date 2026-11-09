/* JARVIS service worker: keeps the app shell, fonts and the offline-AI library available with no internet. */
var CACHE = "jarvis-v3";
var SHELL = ["./", "./index.html", "./manifest.webmanifest"];
var REMOTE_OK = ["fonts.googleapis.com", "fonts.gstatic.com", "cdn.jsdelivr.net"];

self.addEventListener("install", function(e){
  e.waitUntil(caches.open(CACHE).then(function(c){ return c.addAll(SHELL); }).then(function(){ return self.skipWaiting(); }));
});

self.addEventListener("activate", function(e){
  e.waitUntil(
    caches.keys().then(function(keys){
      return Promise.all(keys.filter(function(k){ return k.indexOf("jarvis-") === 0 && k !== CACHE; }).map(function(k){ return caches.delete(k); }));
    }).then(function(){ return self.clients.claim(); })
  );
});

self.addEventListener("fetch", function(e){
  var req = e.request;
  if (req.method !== "GET") return;
  var url = new URL(req.url);
  var sameOrigin = url.origin === self.location.origin;
  if (!sameOrigin && REMOTE_OK.indexOf(url.hostname) === -1) return;

  e.respondWith(
    caches.open(CACHE).then(function(cache){
      return cache.match(req, { ignoreSearch: sameOrigin }).then(function(hit){
        var net = fetch(req).then(function(res){
          if (res && (res.ok || res.type === "opaque")) cache.put(req, res.clone());
          return res;
        }).catch(function(){ return hit; });
        return hit || net;
      });
    })
  );
});
