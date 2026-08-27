import { TypeQuestionEvaluation } from '../constants/type-question-evaluation';

export interface DbQuestion {
  code: string;
  texte: string;
  type: TypeQuestionEvaluation;
  paramsJson?: string | null;
  options?: string[] | null;
  bisQuestion: string;
  bisQuestionLowNotation?: boolean;
  bisQuestionTrue?: boolean;
  bisQuestionFalse?: boolean;
}
