/**
 * ui.js — Utilidades de UI compartidas para ICC Labcontrol.
 *
 * Expone en window:
 *   - toast(mensaje, tipo)   // tipo: 'success' | 'error' | 'info' | 'warning'
 *   - confirmDialog(opts)    // Promise<boolean>, reemplazo de window.confirm()
 *
 * Inyecta automáticamente los contenedores DOM que necesita al cargar.
 * Requiere Tailwind (CDN) y Feather Icons ya cargados.
 */
(function () {
  'use strict';

  // ---------------- Utilidades ----------------
  function escapeHtml(s) {
    return String(s == null ? '' : s).replace(/[&<>"']/g, c => ({
      '&': '&amp;', '<': '&lt;', '>': '&gt;', '"': '&quot;', "'": '&#39;'
    }[c]));
  }

  function refreshIcons() {
    if (window.feather && typeof window.feather.replace === 'function') {
      window.feather.replace();
    }
  }

  // ---------------- Inyección de contenedores ----------------
  function ensureContainers() {
    if (!document.getElementById('iccToasts')) {
      const t = document.createElement('div');
      t.id = 'iccToasts';
      t.className = 'fixed top-4 right-4 z-[100] space-y-2 w-[320px] pointer-events-none';
      document.body.appendChild(t);
    }

    if (!document.getElementById('iccConfirm')) {
      const modal = document.createElement('div');
      modal.id = 'iccConfirm';
      modal.className = 'fixed inset-0 z-[90] hidden bg-black/40 flex items-center justify-center p-4';
      modal.innerHTML = `
        <div class="bg-white rounded-xl shadow-xl w-full max-w-md overflow-hidden">
          <div class="px-5 py-4 border-b border-gray-100 flex items-center gap-3">
            <div id="iccConfirmIconWrap"
                 class="w-10 h-10 rounded-full flex items-center justify-center">
              <i id="iccConfirmIcon" data-feather="alert-triangle" class="w-5 h-5"></i>
            </div>
            <h3 id="iccConfirmTitle" class="text-lg font-bold text-[#4A4A4A]">Confirmar</h3>
          </div>
          <div class="px-5 py-5 space-y-2">
            <p id="iccConfirmMessage" class="text-sm text-gray-600"></p>
            <p id="iccConfirmDetail" class="text-xs text-gray-500 hidden"></p>
          </div>
          <div class="px-5 py-4 bg-gray-50 border-t border-gray-100 flex items-center justify-end gap-2">
            <button type="button" id="iccConfirmCancel"
                    class="px-4 py-2 rounded-lg border border-gray-300 text-gray-600 hover:bg-white text-sm font-semibold">
              Cancelar
            </button>
            <button type="button" id="iccConfirmOk"
                    class="inline-flex items-center gap-1.5 px-4 py-2 text-white rounded-lg text-sm font-semibold">
              <i id="iccConfirmOkIcon" data-feather="check" class="w-4 h-4"></i>
              <span id="iccConfirmOkLabel">Aceptar</span>
            </button>
          </div>
        </div>`;
      document.body.appendChild(modal);
      refreshIcons();
    }
  }

  // ---------------- Toast ----------------
  const TOAST_STYLES = {
    success: { bg: 'bg-green-50', border: 'border-green-200', text: 'text-green-800', icon: 'check-circle', iconColor: 'text-green-600' },
    error:   { bg: 'bg-red-50',   border: 'border-red-200',   text: 'text-red-800',   icon: 'alert-circle', iconColor: 'text-red-600'   },
    warning: { bg: 'bg-amber-50', border: 'border-amber-200', text: 'text-amber-800', icon: 'alert-triangle', iconColor: 'text-amber-600' },
    info:    { bg: 'bg-icc-50',   border: 'border-icc-200',   text: 'text-icc-800',   icon: 'info',          iconColor: 'text-icc-600'   }
  };

  function toast(mensaje, tipo) {
    ensureContainers();
    const s = TOAST_STYLES[tipo] || TOAST_STYLES.info;
    const cont = document.getElementById('iccToasts');

    const el = document.createElement('div');
    el.className = `pointer-events-auto flex items-start gap-2 px-4 py-3 rounded-lg border shadow-sm transition-all duration-300 opacity-0 translate-x-2 ${s.bg} ${s.border} ${s.text}`;
    el.innerHTML = `
      <i data-feather="${s.icon}" class="w-4 h-4 mt-0.5 shrink-0 ${s.iconColor}"></i>
      <div class="flex-1 text-sm">${escapeHtml(mensaje)}</div>
      <button type="button" class="text-gray-400 hover:text-gray-600 shrink-0" aria-label="Cerrar">
        <i data-feather="x" class="w-4 h-4"></i>
      </button>`;
    cont.appendChild(el);
    refreshIcons();

    requestAnimationFrame(() => {
      el.classList.remove('opacity-0', 'translate-x-2');
    });

    let alive = true;
    const cerrar = () => {
      if (!alive) return;
      alive = false;
      el.classList.add('opacity-0', 'translate-x-2');
      setTimeout(() => el.remove(), 300);
    };
    el.querySelector('button').addEventListener('click', cerrar);
    setTimeout(cerrar, 4000);
    return cerrar;
  }

  // ---------------- Confirm dialog ----------------
  /**
   * confirmDialog({ title, message, detail, confirmText, cancelText, variant })
   *   variant: 'danger' (rojo, default) | 'primary' (naranja ICC)
   * Devuelve Promise<boolean>: true si el usuario confirmó, false si canceló.
   */
  function confirmDialog(opts) {
    ensureContainers();
    opts = opts || {};
    const variant = opts.variant === 'primary' ? 'primary' : 'danger';

    const modal       = document.getElementById('iccConfirm');
    const iconWrap    = document.getElementById('iccConfirmIconWrap');
    const icon        = document.getElementById('iccConfirmIcon');
    const titleEl     = document.getElementById('iccConfirmTitle');
    const messageEl   = document.getElementById('iccConfirmMessage');
    const detailEl    = document.getElementById('iccConfirmDetail');
    const okBtn       = document.getElementById('iccConfirmOk');
    const okLabel     = document.getElementById('iccConfirmOkLabel');
    const okIcon      = document.getElementById('iccConfirmOkIcon');
    const cancelBtn   = document.getElementById('iccConfirmCancel');

    titleEl.textContent   = opts.title   || '¿Confirmar acción?';
    messageEl.innerHTML   = escapeHtml(opts.message || '');
    if (opts.detail) {
      detailEl.innerHTML = escapeHtml(opts.detail);
      detailEl.classList.remove('hidden');
    } else {
      detailEl.classList.add('hidden');
      detailEl.textContent = '';
    }
    okLabel.textContent = opts.confirmText || (variant === 'danger' ? 'Sí, eliminar' : 'Aceptar');
    cancelBtn.textContent = opts.cancelText || 'Cancelar';

    // Variantes visuales
    iconWrap.className = 'w-10 h-10 rounded-full flex items-center justify-center '
      + (variant === 'danger' ? 'bg-red-50 text-red-600' : 'bg-icc-50 text-icc-600');
    icon.setAttribute('data-feather', variant === 'danger' ? 'alert-triangle' : 'help-circle');

    okBtn.className = 'inline-flex items-center gap-1.5 px-4 py-2 text-white rounded-lg text-sm font-semibold '
      + (variant === 'danger' ? 'bg-red-600 hover:bg-red-700' : 'bg-icc-500 hover:bg-icc-600');
    okIcon.setAttribute('data-feather', variant === 'danger' ? 'trash-2' : 'check');

    refreshIcons();
    modal.classList.remove('hidden');

    return new Promise(resolve => {
      function cleanup(result) {
        modal.classList.add('hidden');
        okBtn.removeEventListener('click', onOk);
        cancelBtn.removeEventListener('click', onCancel);
        modal.removeEventListener('click', onBackdrop);
        document.removeEventListener('keydown', onKey);
        resolve(result);
      }
      function onOk()      { cleanup(true); }
      function onCancel()  { cleanup(false); }
      function onBackdrop(e) { if (e.target === modal) cleanup(false); }
      function onKey(e)    {
        if (e.key === 'Escape') cleanup(false);
        if (e.key === 'Enter')  cleanup(true);
      }

      okBtn.addEventListener('click', onOk);
      cancelBtn.addEventListener('click', onCancel);
      modal.addEventListener('click', onBackdrop);
      document.addEventListener('keydown', onKey);
    });
  }

  // ---------------- Exports ----------------
  window.toast = toast;
  window.confirmDialog = confirmDialog;

  if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', ensureContainers);
  } else {
    ensureContainers();
  }
})();
