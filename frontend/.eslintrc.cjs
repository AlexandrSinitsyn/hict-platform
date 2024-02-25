/* eslint-env node */
require('@rushstack/eslint-patch/modern-module-resolution')

module.exports = {
  root: true,
  plugins: ['import'],
  'extends': [
    'plugin:vue/vue3-essential',
    'eslint:recommended',
    '@vue/eslint-config-typescript',
    '@vue/eslint-config-typescript/recommended',
    '@vue/eslint-config-prettier'
  ],
  parserOptions: {
    ecmaVersion: 'latest'
  },
  rules: {
    'block-spacing': 'error',
    'no-multi-spaces': 'error',
    'no-trailing-spaces': 'warn',
    '@typescript-eslint/no-inferrable-types': 'warn',
    'prettier/prettier': [
      'warn',
      {
        semi: true,
        indent: 4,
        tabWidth: 4,
        trailingComma: 'es5',
      },
    ],
  },
  settings: {
    'import/resolver': {
      alias: {
        map: [['@', './src']],
        extensions: ['.vue', '.json', '.js', '.ts'],
      },
    },
  }
}
