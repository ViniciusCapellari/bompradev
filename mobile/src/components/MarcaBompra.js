import { Image, View } from 'react-native';

// Posições na arte original de 1600 x 1600, exibida sem alterar o arquivo.
const marcas = {
  principal: { esquerda: 670, topo: 150, lado: 310 },
  drop: { esquerda: 370, topo: 600, lado: 285 },
  lab: { esquerda: 1005, topo: 600, lado: 285 },
  premio: { esquerda: 370, topo: 985, lado: 285 },
  voz: { esquerda: 1005, topo: 985, lado: 285 },
};

const imagem = require('../../assets/marca-original.jpg');

export default function MarcaBompra({ variante = 'principal', tamanho = 64 }) {
  const marca = marcas[variante] || marcas.principal;
  const escala = tamanho / marca.lado;

  return (
    <View accessibilityElementsHidden importantForAccessibility="no-hide-descendants"
      style={{ width: tamanho, height: tamanho, overflow: 'hidden', borderRadius: tamanho * 0.16 }}>
      <Image source={imagem} resizeMode="stretch" accessible={false}
        style={{ position: 'absolute', width: 1600 * escala, height: 1600 * escala,
          left: -marca.esquerda * escala, top: -marca.topo * escala }} />
    </View>
  );
}
