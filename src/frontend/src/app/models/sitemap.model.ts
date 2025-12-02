export interface SitemapMeta {
  label: string;
  group?: string;
  order?: number;
  exclude?: boolean;
}

export interface SitemapItem {
  url: string;
  label: string;
  group?: string;
  order?: number;
}

export interface SitemapGroup {
  name: string;
  items: SitemapItem[];
}

export const SITEMAP_GROUP_ORDER: { [groupName: string]: number } = {
  'Général': 1,
  'Conventions de stages': 2,
  'Conventions en masse': 3,
  'Centres de gestion': 4,
  'Établissements d\'accueil': 5,
  'Nomenclatures': 6,
  'Paramétrage global': 7,
};
