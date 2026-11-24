import { HelperText, TextInput } from 'react-native-paper';

import { colors } from '../theme';

export default function CampoTexto({ label, valor, setValor, erro, ...rest }) {
  return (
    <>
      <TextInput label={label} mode="outlined" value={valor} onChangeText={setValor}
        error={Boolean(erro)} style={{ marginBottom: erro ? 0 : 14 }}
        theme={{ colors: { background: colors.surface } }} {...rest} />
      {erro ? <HelperText type="error">{erro}</HelperText> : null}
    </>
  );
}
