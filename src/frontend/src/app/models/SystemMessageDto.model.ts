export interface SystemMessageDto {
  id?: number;
  messageContent: string;
  startDate: string | Date;
  endDate: string | Date;
  active: boolean;
}
