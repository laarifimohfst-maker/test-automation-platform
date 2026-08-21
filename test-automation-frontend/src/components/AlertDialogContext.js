import { createContext, useContext } from 'react';

export const AlertDialogContext = createContext(null);

export function useAlertDialog() {
  const contexte = useContext(AlertDialogContext);

  if (!contexte) {
    throw new Error(
      'useAlertDialog doit être utilisé dans AlertDialogProvider.'
    );
  }

  return contexte;
}
