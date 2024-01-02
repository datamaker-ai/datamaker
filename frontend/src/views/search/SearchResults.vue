<template>
  <CCard>
    <CCardHeader>
      <CIcon name="cil-justify-center"/>
      <strong>{{ searchQuery }}</strong>
    </CCardHeader>
    <CCardBody>
      <!--
      <CListGroup>
        <CListGroupItem href="#some-link" v-if="results.length === 0">No results</CListGroupItem>
        <template v-for="result in results">
          <CListGroupItem v-bind:to="result.url">{{result.name}} -- {{result.type}} (icon)</CListGroupItem>
        </template>
      </CListGroup>
      -->
      <DataTable
              hover
              outlined
              sorter
              :items="results"
              :fields="fields"
              :items-per-page="25"
              pagination
              index-column
              clickable-rows
              @row-clicked="rowClicked"
              :loading="isLoading"
      >
      </DataTable>
    </CCardBody>
  </CCard>
</template>

<script>
  import {api} from "../../constants/api"
  import DataTable from '../../components/table/DataTable'
  import {i18n} from "@/plugins/i18n";

  export default {
    name: "SearchResults",
    components: {
      DataTable
    },
    data() {
      return {
        searchQuery: this.$route.query.query,
        results: [],
        fields: [
          {
            key: 'name',
            label: i18n.t('common.name')
          },
          {
            key: 'description',
            label: i18n.t('common.description')
          },
          {
            key: 'type',
            label: i18n.t('common.type')
          },
          {
            key: 'text',
            label: i18n.t('search.index')
          }
        ],
        isLoading: false
      }
    },
    mounted() {
      this.refresh(this.$route.query.query)
    },
    methods: {
      getIcon() {

      },
      refresh(query) {
        this.searchQuery = query
        this.isLoading = true
        if (query && query.length >= 3) {
          api.get('/search?query=' + query)
          .then(response => {
            let results = response.data.payload
            results.forEach(r => {
              let re = new RegExp(query,"gi");
              let matchedQuery = query.replace('*', '').replace('?', '')
              r.name = r.name.replace(re, `<span class='highlight'>${matchedQuery}</span>`)
              if (r.description) {
                r.description = r.description.replace(re, `<span class='highlight'>${matchedQuery}</span>`)
              }

              r.text = ''
              for (const [key, value] of Object.entries(r.fields)) {
                if (value) {
                  r.text = r.text + key + ': ' + value.toString().replace(re, `<span class='highlight'>${matchedQuery}</span>`) + '<br/>'
                }
              }
            })
            this.results = results
            this.isLoading = false
          }).catch(err => {
            console.log(err)
            this.isLoading = false
          })
        } else {
          this.isLoading = false
          this.results = []
        }
      },
      rowClicked (item, index) {
        document.querySelector('#search').value = ''
        this.$router.push({path: item.url})
      }
    },
    watch: {
      '$route.query.query': function (newValue, oldValue) {
        this.refresh(newValue)
      }
    }
  }
</script>

<style>
  .highlight {background-color: yellow}
</style>