/**
 * Copie un texte dans le presse-papier, avec repli sur document.execCommand pour les
 * navigateurs ou les contextes non sécurisés où navigator.clipboard est indisponible.
 */
export function copyToClipboard(text: string): Promise<boolean> {
  if (!text) {
    return Promise.resolve(false);
  }
  if (navigator?.clipboard?.writeText) {
    return navigator.clipboard.writeText(text).then(
      () => true,
      () => fallbackCopy(text)
    );
  }
  return Promise.resolve(fallbackCopy(text));
}

function fallbackCopy(text: string): boolean {
  const el = document.createElement('textarea');
  el.value = text;
  el.style.cssText = 'position:fixed;top:-9999px;left:-9999px;opacity:0';
  document.body.appendChild(el);
  el.select();
  try {
    return document.execCommand('copy');
  } catch {
    return false;
  } finally {
    document.body.removeChild(el);
  }
}
