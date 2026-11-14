const toggle = document.querySelector('.menu-toggle');
const menu = document.querySelector('#mobile-nav');
if (toggle && menu) {
  const setOpen = (open) => {
    menu.hidden = !open;
    toggle.setAttribute('aria-expanded', String(open));
    toggle.setAttribute('aria-label', open ? 'Close navigation' : 'Open navigation');
  };
  toggle.addEventListener('click', () => setOpen(toggle.getAttribute('aria-expanded') !== 'true'));
  menu.addEventListener('click', (event) => { if (event.target.closest('a')) setOpen(false); });
  document.addEventListener('keydown', (event) => { if (event.key === 'Escape' && !menu.hidden) { setOpen(false); toggle.focus(); } });
}
const year = document.querySelector('#year');
if (year) year.textContent = String(new Date().getFullYear());

// The HTML is usable without JavaScript or the API, with verified v1.6 links.
// When possible, use the newest complete versioned release, including pre-releases.
// Ignore rolling previews and incomplete releases; never mix files from different releases.
const patterns = {
  arm64: /^TurboCraft-Launcher-.*-arm64-v8a\.apk$/i,
  armv7: /^TurboCraft-Launcher-.*-armeabi-v7a\.apk$/i,
  universal: /^TurboCraft-Launcher-.*-universal\.apk$/i,
  x86: /^TurboCraft-Launcher-.*-x86\.apk$/i,
  x86_64: /^TurboCraft-Launcher-.*-x86_64\.apk$/i,
};
function completeRelease(release) {
  if (release.draft || !/^v\d+(?:\.\d+)*(?:[-.][\w.-]+)?$/i.test(release.tag_name || '') || !Array.isArray(release.assets)) return null;
  const assets = Object.fromEntries(Object.entries(patterns).map(([key, pattern]) => [key, release.assets.find(asset => pattern.test(asset.name))]));
  const checksums = release.assets.find(asset => asset.name === 'SHA256SUMS.txt');
  return Object.values(assets).every(Boolean) && checksums ? { release, assets, checksums } : null;
}
async function refreshDownloads() {
  try {
    const response = await fetch('https://api.github.com/repos/TCL-Team/TurboCraft-Launcher/releases?per_page=100', { headers: { Accept: 'application/vnd.github+json' } });
    if (!response.ok) return;
    const list = await response.json();
    if (!Array.isArray(list)) return;
    const available = list.map(completeRelease).filter(Boolean).sort((a, b) => Date.parse(b.release.published_at) - Date.parse(a.release.published_at));
    const chosen = available[0];
    if (!chosen) return;
    const { release, assets, checksums } = chosen;
    const base = `https://github.com/TCL-Team/TurboCraft-Launcher/releases/download/${encodeURIComponent(release.tag_name)}/`;
    Object.entries(assets).forEach(([key, asset]) => {
      const url = base + encodeURIComponent(asset.name);
      document.querySelectorAll(`[data-asset="${key}"]`).forEach(link => { link.href = url; });
      if (Number.isFinite(asset.size) && asset.size > 0) document.querySelectorAll(`[data-size="${key}"]`).forEach(node => { node.textContent = `${(asset.size / 1048576).toFixed(1)} MiB`; });
    });
    document.querySelectorAll('[data-release-label]').forEach(node => { node.textContent = `VERSION ${release.tag_name.replace(/^v/i, '')}`; });
    document.querySelectorAll('[data-release-status]').forEach(node => { node.textContent = release.prerelease ? 'PRE-RELEASE' : 'RELEASE'; });
    document.querySelectorAll('[data-release-notes]').forEach(link => { link.href = `https://github.com/TCL-Team/TurboCraft-Launcher/releases/tag/${encodeURIComponent(release.tag_name)}`; });
    document.querySelectorAll('[data-checksums]').forEach(link => { link.href = base + encodeURIComponent(checksums.name); });
    const note = document.querySelector('#release-note');
    if (note) note.textContent = `Showing ${release.tag_name}${release.prerelease ? ' pre-release' : ''}.`;
  } catch (_) {
    // Keep the verified static fallback if GitHub is unreachable or rate-limited.
  }
}
refreshDownloads();
