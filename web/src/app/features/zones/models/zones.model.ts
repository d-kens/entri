export type Zone = {
  id: number;
  zoneName: string;
  city: string;
  isActive: boolean;
  isCbd: boolean;
  createdAt: string;
  updatedAt: string;
}

export type Agent = {
  id: number;
  name: string;
  zoneId: number;
  latitude: number;
  longitude: number;
  addressDescription: string;
  isActive: boolean;
  phone: string;
  openingTime: string;
  closingTime: string;
  createdAt: string;
  updatedAt: string;
}
