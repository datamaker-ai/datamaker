<template>
  <CCard>
    <CCardHeader>
      {{ $t('mappings.header') }}
      <div class="float-right">
        <CButton to="mappings/create" class="mr-1" color="primary" :disabled="disabledButtons">{{ $t('mappings.create_button') }}</CButton>
      </div>
    </CCardHeader>
    <CCardBody>
      <CDataTable
          hover
          :fields="fields"
          :items="items"
          :items-per-page="perPage"
          pagination
          column-filter
          :table-filter="{label: $t('table.filter_label'), placeholder: $t('table.filter_placeholder')}"
          index-column
          striped
          sorter
          :items-per-page-select="{label: $t('table.items_label')}"
          :clickable-rows=false
          :loading="isLoading"
      >
        <template #edit_details="{item, index}">
          <td class="py-2">
            <CButton v-bind:to="{path:'mappings/edit/' + item.externalId, query: {name: item.name}}"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     class="mr-1"
                     v-c-tooltip="{content: $t('mappings.add_remove_fields_tooltip')}"
                     :disabled="disabledButtons"
            >
              {{ $t('common.edit_button') }}
            </CButton>
            <CButton @click="showDeleteWarning(item, index)"
                     color="primary"
                     variant="outline"
                     square
                     size="sm"
                     :disabled="disabledButtons"
            >
              {{ $t('common.delete_button') }}
            </CButton>
          </td>
        </template>
      </CDataTable>
    </CCardBody>
    <!--
    <CCardFooter>
      <CButton to="mappings/create" class="mr-1" color="primary" :disabled="disabledButtons">Create field mapping</CButton>
    </CCardFooter>
    -->
    <CModal
        :title="$t('common.delete_warning_title')"
        color="danger"
        :show.sync="dangerModal"
    >
      {{ $t('common.delete_warning_message', { item: currentItem ? currentItem.name : '' }) }}
      <template #footer>
        <CButton @click="dangerModal = false" color="danger">{{ $t('common.discard_action') }}</CButton>
        <CButton @click="deleteItem(currentItem, rowNumber)" color="success">{{ $t('common.accept_action') }}</CButton>
      </template>
    </CModal>

  </CCard>
</template>

<script>
  import { api, getComponents } from "../../constants/api";
  import {i18n} from "@/plugins/i18n";

  export default {
    name: "FieldMappings",
    data() {
      return {
        fields: [
          {
            key: 'name',
            label: i18n.t('common.name')
          },
          {
            key: 'languageTag',
            label: i18n.t('common.language')
          },
          {
            key: 'className',
            label: i18n.t('common.classname')
          },
          {
            key: 'formatterClassName',
            label: i18n.t('common.formatter_classname')
          },
          {
            key: 'edit_details',
            label: '',
            _style: 'width:20%',
            sorter: false,
            filter: false
          }
        ],
        items: [],
        perPage: 5,
        isLoading: false,
        disabledButtons: false,
        dangerModal: false,
        currentItem: null,
        rowNumber: -1
      }
    },
    mounted() {
      this.isLoading = true
      api.get('/field-mappings')
      .then(response => {
        if (response.data && response.data.payload.length > 0) {
          this.items = response.data.payload
        }
        this.isLoading = false
      })
      .catch(err => {})
    },
    methods: {
      showDeleteWarning(item, index) {
        this.dangerModal = true
        this.currentItem = item
        this.rowNumber = index
      },
      deleteItem(item, index) {
        this.dangerModal = false
        this.isLoading = true

        api.delete("/field-mappings/" + item.externalId)
        .then(response => {
          this.disabledButtons = false
          this.isLoading = false
          this.items.splice(this.items.indexOf(item), 1)
        }).catch(err => {
          this.disabledButtons = false
          this.isLoading = false
        })
      },
    },
    paginationProps: {
      align: 'center',
      doubleArrows: false,
      previousButtonHtml: 'prev',
      nextButtonHtml: 'next'
    }
  }
</script>

<style scoped>

</style>