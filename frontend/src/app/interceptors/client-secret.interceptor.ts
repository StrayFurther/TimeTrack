import { HttpInterceptorFn } from '@angular/common/http';
import * as CryptoJS from 'crypto-js';
import { environment } from '../../env/env';

export const clientSecretInterceptor: HttpInterceptorFn = (req, next) => {
  // Skip modifying `OPTIONS` requests
  if (req.method === 'OPTIONS') {
    return next(req);
  }

  const nonce = `${Date.now().toString()}-${Math.random().toString(36).substring(2, 15)}`;
  const timestamp = new Date().toISOString();
  const signature = CryptoJS.HmacSHA256(`${nonce}:${timestamp}`, environment.clientSecret).toString();

  const modifiedRequest = req.clone({
    setHeaders: {
      'X-Client-Nonce': nonce,
      'X-Client-Timestamp': timestamp,
      'X-Client-Signature': signature,
    },
  });

  return next(modifiedRequest);
};
