import { Directive } from '@angular/core';

@Directive()
export abstract class BaseComponent {
  private _className = '';
  protected _testId?: string;

  get className(): string {
    return this._className;
  }

  set className(value: string) {
    this._className = value;
  }

  get testId(): string | undefined {
    return this._testId;
  }

  set testId(value: string | undefined) {
    this._testId = value;
  }
}