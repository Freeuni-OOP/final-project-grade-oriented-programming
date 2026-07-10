// src/i18n/index.js
import i18n from 'i18next';
import { initReactI18next } from 'react-i18next';
import LanguageDetector from 'i18next-browser-languagedetector';

// English imports
import enCommon from './locales/en/common.json';
import enCustomer from './locales/en/customer.json';
import enCards from './locales/en/cards.json';
import enAccounts from './locales/en/accounts.json';

// Georgian imports
import kaCommon from './locales/ka/common.json';
import kaCustomer from './locales/ka/customer.json';
import kaCards from './locales/ka/cards.json';
import kaAccounts from './locales/ka/accounts.json';

// Chinese imports
import zhCommon from './locales/zh/common.json';
import zhCustomer from './locales/zh/customer.json';
import zhCards from './locales/zh/cards.json';
import zhAccounts from './locales/zh/accounts.json';

i18n
  .use(LanguageDetector) // reads browser language automatically
  .use(initReactI18next)
  .init({
    debug: true,
    fallbackLng: 'ka', // uses Georgian if detected language has no translation
    defaultNS: 'common',
    resources: {
      en: {
        common: enCommon,
        customer: enCustomer,
        cards: enCards,
        accounts: enAccounts,
      },
      ka: {
        common: kaCommon,
        customer: kaCustomer,
        cards: kaCards,
        accounts: kaAccounts,
      },
      zh: {
        common: zhCommon,
        customer: zhCustomer,
        cards: zhCards,
        accounts: zhAccounts,
      },
    },
    interpolation: {
      escapeValue: false, // React already escapes values
    },
  });

export default i18n;
