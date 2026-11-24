import { Stack } from 'expo-router';
import { StatusBar } from 'expo-status-bar';
import { MD3LightTheme, PaperProvider } from 'react-native-paper';

import { AuthProvider } from '../context/AuthContext';
import { colors } from '../theme';

export default function RootLayout() {
  return (
    <PaperProvider theme={{ ...MD3LightTheme, colors: { ...MD3LightTheme.colors, primary: colors.primary, secondary: colors.accent } }}>
      <AuthProvider>
      <StatusBar style="light" />
      <Stack
        screenOptions={{
          contentStyle: { backgroundColor: colors.background },
          headerStyle: { backgroundColor: colors.primary },
          headerTintColor: colors.onPrimary,
          headerTitleStyle: { fontWeight: '600' },
        }}
      >
        <Stack.Screen name="index" options={{ headerShown: false }} />
        <Stack.Screen name="login" options={{ title: 'Acessar conta' }} />
        <Stack.Screen name="cadastro" options={{ title: 'Criar conta' }} />
        <Stack.Screen name="perfil" options={{ title: 'Meu perfil' }} />
        <Stack.Screen name="disponibilidade" options={{ title: 'Minha disponibilidade' }} />
        <Stack.Screen name="agendamento" options={{ title: 'Solicitar agendamento' }} />
        <Stack.Screen name="meus-agendamentos" options={{ title: 'Meus agendamentos' }} />
        <Stack.Screen name="solicitacoes" options={{ title: 'Solicitações recebidas' }} />
        <Stack.Screen name="perfil/[id]" options={{ title: 'Perfil público' }} />
        <Stack.Screen name="secoes/[id]" options={{ title: 'BompraDev' }} />
        <Stack.Screen name="servicos/index" options={{ title: 'Serviços' }} />
        <Stack.Screen name="servicos/formulario" options={{ title: 'Serviço' }} />
        <Stack.Screen name="servicos/[id]" options={{ title: 'Detalhes do serviço' }} />
      </Stack>
      </AuthProvider>
    </PaperProvider>
  );
}

