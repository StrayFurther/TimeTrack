import {computed, Injectable, Signal, signal } from '@angular/core';

@Injectable({
  providedIn: 'root'
})
export class LoadingSpinnerService {
  private _showSpinner = signal(false);
  private _spinnerMessage = signal<string | null>(null);

  private _showSpinnerListener = computed(() => this._showSpinner());
  private _spinnerMessageListener = computed(() => this._spinnerMessage());

  public set showSpinner(loading: boolean) {
    this._showSpinner.set(loading);
  }

  public set message(message: string | null) {
    this._spinnerMessage.set(message);
  }

  public get showSpinnerListener(): Signal<boolean>  {
    return this._showSpinnerListener;
  }

  public get spinnerMessageListener(): Signal<string | null> {
    return this._spinnerMessageListener;
  }

  public clearSpinner(): void {
    this._showSpinner.set(false);
    this._spinnerMessage.set(null);
  }

}
