<template>
    <CCard>
        <CCardHeader>
          {{ $t('sinks.header') }}
            <div class="float-right">
                <CButton to="sinks/create" class="mr-1" color="primary" :disabled="disabledButtons">{{ $t('sinks.create_sink_button') }}</CButton>
            </div>
        </CCardHeader>
        <CCardBody>
            <CDataTable
                    hover
                    :items="items"
                    :fields="fields"
                    :items-per-page="perPage"
                    pagination
                    column-filter
                    :table-filter="{label: $t('table.filter_label'), placeholder: $t('table.filter_placeholder')}"
                    index-column
                    sorter
                    :items-per-page-select="{label: $t('table.items_label')}"
                    :clickable-rows=false
                    :loading="isLoading"
            >
                <template #edit_details="{item, index}">
                    <td class="py-2">
                        <CButton v-bind:to="{path:'sinks/edit/' + item.externalId, query: {name: item.name}}"
                                 color="primary"
                                 variant="outline"
                                 square
                                 size="sm"
                                 class="mr-1"
                                 v-c-tooltip="'Add/remove fields'"
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
            <CButton to="sinks/create" class="mr-1" color="primary" :disabled="disabledButtons">Create sink configuration</CButton>
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
    import {api} from "../../constants/api";
    import {i18n} from "@/plugins/i18n";

    export default {
        name: 'SinkConfigurations',
        data: () => {
            return {
                items: [],
                fields: [
                    {
                      key: 'name',
                      label: i18n.t('common.name')
                    },
                    {
                      key: 'sinkClassName',
                      label: i18n.t('sinks.classname')
                    },
                    {
                      key: 'dateCreated',
                      label: i18n.t('common.created')
                    },
                    {
                      key: 'workspaceName',
                      label: i18n.t('common.workspace')
                    },
                    {
                        key: 'edit_details',
                        label: '',
                        _style: 'width:20%',
                        sorter: false,
                        filter: false
                    }
                ],
                perPage: 10,
                isLoading: false,
                disabledButtons: false,
                dangerModal: false,
                currentItem: null,
                rowNumber: -1
            }
        },
        mounted() {
            this.isLoading = true
            api.get("/sinks")
                .then(response => {
                    this.items = response.data.payload
                    this.isLoading = false
                })
                .catch(err => {})
        },
        paginationProps: {
            align: 'center',
            doubleArrows: false,
            previousButtonHtml: 'prev',
            nextButtonHtml: 'next'
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

                api.delete("/sinks/" + item.externalId)
                    .then(response => {
                        this.disabledButtons = false
                        this.isLoading = false
                        this.items.splice(this.items.indexOf(item), 1)
                    }).catch(err => {
                    this.disabledButtons = false
                    this.isLoading = false
                })
            },
            getBadge (status) {
                return status === 'Active' ? 'success'
                    : status === 'Inactive' ? 'secondary'
                        : status === 'Pending' ? 'warning'
                            : status === 'Banned' ? 'danger' : 'primary'
            },
            datasetLink (id) {
                return `datasets/${id.toString()}`
            },
            rowClicked (item, index) {
                const datasetLink = this.datasetLink(item.externalId)
                this.$router.push({path: datasetLink, params: {name: item.name}})
            }
        }
    }
</script>


<style scoped>

</style>