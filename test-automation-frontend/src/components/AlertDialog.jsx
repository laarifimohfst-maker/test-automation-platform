import {
  AlertTriangle,
  CheckCircle2,
  Info,
  X,
  XCircle,
} from 'lucide-react';
import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';
import { AlertDialogContext } from './AlertDialogContext';
import './AlertDialog.css';

const ICONES = {
  danger: AlertTriangle,
  error: XCircle,
  success: CheckCircle2,
  info: Info,
};

export function AlertDialogProvider({ children }) {
  const [dialogue, setDialogue] = useState(null);
  const reponseRef = useRef(null);

  const ouvrirDialogue = useCallback((options) => {
    if (reponseRef.current) {
      reponseRef.current(false);
    }

    return new Promise((resolve) => {
      reponseRef.current = resolve;
      setDialogue(options);
    });
  }, []);

  const demanderConfirmation = useCallback(
    (options) =>
      ouvrirDialogue({
        mode: 'confirmation',
        variante: 'danger',
        titre: 'Confirmer cette action',
        texteConfirmation: 'Confirmer',
        texteAnnulation: 'Annuler',
        ...options,
      }),
    [ouvrirDialogue]
  );

  const afficherAlerte = useCallback(
    (options) =>
      ouvrirDialogue({
        mode: 'alerte',
        variante: 'info',
        titre: 'Information',
        texteConfirmation: 'Compris',
        ...options,
      }),
    [ouvrirDialogue]
  );

  const fermerDialogue = useCallback((reponse) => {
    const resoudre = reponseRef.current;
    reponseRef.current = null;
    setDialogue(null);
    resoudre?.(reponse);
  }, []);

  useEffect(() => {
    if (!dialogue) return undefined;

    const ancienOverflow = document.body.style.overflow;
    document.body.style.overflow = 'hidden';

    const gererClavier = (event) => {
      if (event.key === 'Escape') {
        fermerDialogue(false);
      }
    };

    window.addEventListener('keydown', gererClavier);

    return () => {
      document.body.style.overflow = ancienOverflow;
      window.removeEventListener('keydown', gererClavier);
    };
  }, [dialogue, fermerDialogue]);

  const valeurContexte = useMemo(
    () => ({ demanderConfirmation, afficherAlerte }),
    [demanderConfirmation, afficherAlerte]
  );

  const Icone = dialogue ? ICONES[dialogue.variante] || Info : Info;

  return (
    <AlertDialogContext.Provider value={valeurContexte}>
      {children}

      {dialogue && (
        <div
          className="app-alert-overlay"
          role="presentation"
          onMouseDown={(event) => {
            if (event.target === event.currentTarget) {
              fermerDialogue(false);
            }
          }}
        >
          <section
            className={`app-alert app-alert-${dialogue.variante}`}
            role="alertdialog"
            aria-modal="true"
            aria-labelledby="app-alert-title"
            aria-describedby="app-alert-message"
          >
            <button
              type="button"
              className="app-alert-close"
              aria-label="Fermer"
              onClick={() => fermerDialogue(false)}
            >
              <X size={18} />
            </button>

            <div className="app-alert-content">
              <span className="app-alert-icon" aria-hidden="true">
                <Icone size={25} strokeWidth={2.1} />
              </span>

              <div>
                <h2 id="app-alert-title">{dialogue.titre}</h2>
                <p id="app-alert-message">{dialogue.message}</p>
              </div>
            </div>

            <div className="app-alert-actions">
              {dialogue.mode === 'confirmation' && (
                <button
                  type="button"
                  className="app-alert-button app-alert-button-secondary"
                  autoFocus
                  onClick={() => fermerDialogue(false)}
                >
                  {dialogue.texteAnnulation}
                </button>
              )}

              <button
                type="button"
                className="app-alert-button app-alert-button-primary"
                autoFocus={dialogue.mode !== 'confirmation'}
                onClick={() => fermerDialogue(true)}
              >
                {dialogue.texteConfirmation}
              </button>
            </div>
          </section>
        </div>
      )}
    </AlertDialogContext.Provider>
  );
}
