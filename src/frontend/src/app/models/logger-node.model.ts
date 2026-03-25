import {LoggerLevel} from "./logger-level.model";

export interface LoggerNode {
  label: string;
  fullName: string;
  logger?: LoggerLevel;
  children: LoggerNode[];
}
