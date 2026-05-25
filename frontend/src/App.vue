<template>
  <router-view />
</template>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;
  color: #1D2129;
  background: #F2F3F5;
  -webkit-text-size-adjust: 100%;
}

a {
  text-decoration: none;
  color: inherit;
}

@media (max-width: 767px) {
  body {
    font-size: 14px;
  }

  input, select, textarea, button {
    font-size: 14px;
  }
}
</style>
