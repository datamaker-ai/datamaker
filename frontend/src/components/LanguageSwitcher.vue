<template>
    <CSelect
        :label="$t('header.lang')"
        name="language"
        @update:value="changeLanguage"
        :value.sync="currentLanguage"
        :options="supportedLanguages"
        horizontal
    >
    </CSelect>
</template>
<script>
  import { Translation } from '../plugins/Translation'
  import {refreshComponents} from "@/constants/api";

  export default {
    data () {
      return {
        currentLanguage: Translation.currentLanguage
      }
    },
    computed: {
      supportedLanguages () {
        return Translation.supportedLanguages
      }
    },
    methods: {
      changeLanguage (lang) {
        //const lang = e.target.value
        //const to = this.$router.resolve({ params: { lang } })

        return Translation.changeLanguage(lang).then(() => {
          //this.$router.go(0)
          //vm.$forceUpdate();
          refreshComponents()
          this.$eventHub.$emit('change-language', lang)
        })
      },
      isCurrentLanguage (lang) {
        return lang === this.currentLanguage
      }
    }
  }
</script>

<style>
  select {
    width: 15rem;
  }
  .LanguageSwitcher label {
    color: rgba(0, 0, 21, 0.5);
    margin-bottom: 0;
    margin-right: 4px;
  }
</style>