import * as SecureStore from 'expo-secure-store';
import { Platform } from 'react-native';

const SESSION_KEY = 'bompradev.session';

export function salvarSessao(token) {
  if (Platform.OS === 'web') {
    if (typeof window !== 'undefined') window.sessionStorage.setItem(SESSION_KEY, token);
    return Promise.resolve();
  }
  return SecureStore.setItemAsync(SESSION_KEY, token);
}

export function obterSessao() {
  if (Platform.OS === 'web') {
    return Promise.resolve(typeof window === 'undefined' ? null : window.sessionStorage.getItem(SESSION_KEY));
  }
  return SecureStore.getItemAsync(SESSION_KEY);
}

export function removerSessao() {
  if (Platform.OS === 'web') {
    if (typeof window !== 'undefined') window.sessionStorage.removeItem(SESSION_KEY);
    return Promise.resolve();
  }
  return SecureStore.deleteItemAsync(SESSION_KEY);
}

