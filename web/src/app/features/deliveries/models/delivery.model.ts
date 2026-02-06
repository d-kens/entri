export interface Zone {
  id: number;
  zoneName: string;
  city: string;
  isActive: boolean;
}

export interface ParcelPoint {
  id: number;
  name: string;
  zoneId: number;
  agentId: number;
  latitude: number;
  longitude: number;
  addressDescription: string;
  isActive: boolean;
  phone: string;
  openingTime: string;
  closingTime: string;
}

export interface Recipient {
  name: string;
  phoneNumber: string;
}

export interface ParcelDetails {
  name: string;
  price: number;
  description: string;
}

export interface CashOnDelivery {
  enabled: boolean;
  amount?: number;
}

export interface DeliveryLocation {
  zone?: Zone;
  parcelPoint?: ParcelPoint;
}

export interface NewDeliveryForm {
  recipient: Recipient;
  from: DeliveryLocation;
  to: DeliveryLocation;
  parcelDetails: ParcelDetails;
  cashOnDelivery: CashOnDelivery;
}

export interface DeliveryFee {
  amount: number;
  currency: string;
}
