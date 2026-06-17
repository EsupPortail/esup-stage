export class CadreStageConventionPayload {
  etudiantLogin: string | null = null;
  numEtudiant: string | null = null;
  codeEtape: string | null = null;
  codeVersionEtape: string | null = null;
  libelleEtape: string | null = null;
  codeComposante: string | null = null;
  libelleComposante: string | null = null;
  idTypeConvention: number | null = null;
  codeLangueConvention: string | null = null;
  adresseEtudiant: string | null = null;
  codePostalEtudiant: string | null = null;
  villeEtudiant: string | null = null;
  paysEtudiant: string | null = null;
  courrielPersoEtudiant: string | null = null;
  telEtudiant: string | null = null;
  telPortableEtudiant: string | null = null;
  annee: string | null = null;
  codeElp: string | null = null;
  libelleELP: string | null = null;
  creditECTS: number | null = null;
  volumeHoraireFormation: string | null = null;
  libelleCPAM: string | null = null;
  regionCPAM: string | null = null;
  adresseCPAM: string | null = null;

  static from(data: any): CadreStageConventionPayload {
    const payload = new CadreStageConventionPayload();
    const codeFields = ['codeEtape', 'codeVersionEtape', 'codeComposante', 'codeLangueConvention', 'codeElp'];

    [
      'etudiantLogin',
      'numEtudiant',
      'codeEtape',
      'codeVersionEtape',
      'libelleEtape',
      'codeComposante',
      'libelleComposante',
      'codeLangueConvention',
      'adresseEtudiant',
      'codePostalEtudiant',
      'villeEtudiant',
      'paysEtudiant',
      'courrielPersoEtudiant',
      'telEtudiant',
      'telPortableEtudiant',
      'annee',
      'codeElp',
      'libelleELP',
      'volumeHoraireFormation',
      'libelleCPAM',
      'regionCPAM',
      'adresseCPAM',
    ].forEach((field: string) => {
      (payload as any)[field] = CadreStageConventionPayload.toFormString(
        data[field],
        codeFields.includes(field) ? 'code' : 'label'
      );
    });

    payload.idTypeConvention = CadreStageConventionPayload.toNumber(data.idTypeConvention);
    payload.creditECTS = CadreStageConventionPayload.toNumber(data.creditECTS);

    return payload;
  }

  static toFormString(value: any, preference: 'code' | 'label' = 'label'): string | null {
    if (value === null || value === undefined) {
      return null;
    }
    if (typeof value === 'string') {
      return value;
    }
    if (typeof value === 'number' || typeof value === 'boolean') {
      return String(value);
    }
    if (preference === 'code') {
      return CadreStageConventionPayload.toPrimitiveString(value.code
        ?? value.id?.code
        ?? value.value
        ?? value.id
        ?? value.libelle
        ?? value.label
        ?? value.nom
        ?? value.name
        ?? '');
    }
    return CadreStageConventionPayload.toPrimitiveString(value.libelle
      ?? value.label
      ?? value.value
      ?? value.nom
      ?? value.name
      ?? value.code
      ?? value.id?.code
      ?? '');
  }

  private static toNumber(value: any): number | null {
    if (value === null || value === undefined || value === '') {
      return null;
    }
    const numericValue = Number(value);
    return Number.isNaN(numericValue) ? null : numericValue;
  }

  private static toPrimitiveString(value: any): string {
    if (value === null || value === undefined) {
      return '';
    }
    if (typeof value === 'object') {
      return '';
    }
    return String(value);
  }
}
