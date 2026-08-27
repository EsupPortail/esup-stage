import {LoggerLevel} from "./logger-level.model";

export interface FlatLoggerNode {
  label: string;
  fullName: string;
  logger?: LoggerLevel;
  level: number;
  expandable: boolean;
}
