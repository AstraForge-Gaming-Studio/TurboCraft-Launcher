# TurboCraft website

This is a standalone static website. Serve the `website/` directory with any
static host (for example GitHub Pages) or preview it locally:

```sh
python3 -m http.server 8000 --directory website
```

APK buttons directly download GitHub Release assets. The page works without
JavaScript using the verified v1.6 links in `index.html`. With JavaScript, it
queries the public GitHub Releases API and selects the newest published
versioned release (including pre-releases) that contains all five APKs and
`SHA256SUMS.txt`. Rolling preview builds and incomplete releases are skipped;
all displayed links and sizes come from the same release. If the API is
unavailable, rate-limited or blocked, the v1.6 fallback stays in place. Update
the fallback links in `index.html` when appropriate. The Discord invite is also
configured in `index.html`.

The `Deploy website to GitHub Pages` workflow publishes this directory on each
push to `main`. Set the repository's Pages source to **GitHub Actions** before
running it. The site uses relative asset paths, so it also works under a
repository Pages URL.
