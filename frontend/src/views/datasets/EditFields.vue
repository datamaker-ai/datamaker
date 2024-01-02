<template>
    <CCard>
        <CCardHeader>
            <template v-if="$route.query.type">
                {{ $t('dataset.edit_nested_field_header', { name: $route.query.name }) }}
            </template>
            <template v-else>
                {{ $t('dataset.edit_dataset_header', { name: datasetName }) }}
            </template>

            <CButton
                    color="primary"
                    :disabled="disabledButtons"
                    v-if="$route.params.fieldId"
                    @click="submitForm(parent, 'top')"
            >
              {{ $t('dataset.go_back_button') }}
            </CButton>
            <div class="float-right">
                <template v-if="$route.query.type !== 'array' || ($route.query.type === 'array' && items.length === 0)">
                    <CButton @click="addField" color="primary" class="mr-2" :disabled="disabledButtons">{{ $t('dataset.add_field_button') }}</CButton>
                </template>
                <CButton @click="submitForm()" color="primary" :disabled="disabledButtons">{{ $t('common.save_button') }}</CButton>
            </div>
        </CCardHeader>
        <CCardBody>
            <CAlert :show="displayError" color="danger">
                <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
                <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
            </CAlert>
            <CForm>
                <DataTable
                        hover
                        :items="items"
                        :fields="fields"
                        :items-per-page="perPage"
                        :items-per-page-select="{label: $t('table.items_label')}"
                        pagination
                        index-column
                        striped
                        :loading="isLoading"
                >
                    <template #details="{item, index}">
                        <CRow>
                            <CCol col="12" lg="6">
                                <CForm v-if="rowNumber === index && showDetails" class="m-3">
                                    <template v-if="item.className"> <!-- && fieldOptions.length > 0 -->
                                        <div>
                                            <span><strong>{{ $t('common.field_options') }}</strong></span>
                                        </div>
                                        <FieldOptions @redirect-nested="submitForm(item, 'bottom')" :item="item" :current-item="index" :items="items" :fieldOptions="fieldOptions"></FieldOptions>
                                    </template>
                                    <template v-if="item.formatterClassName && formatterOptions.length > 0">
                                        <div>
                                            <span><strong>{{ $t('common.formatter_options') }}</strong></span>
                                        </div>
                                        <FieldOptions :item="item" :current-item="index" :items="items" :fieldOptions="formatterOptions"></FieldOptions>
                                    </template>
                                </CForm>
                            </CCol>
                        </CRow>
                    </template>
                    <template #name="{item}">
                        <td class="py-2">
                            <CInput v-model="item.name"
                                    horizontal
                                    :placeholder="$t('dataset.change_name_placeholder')"
                                    required
                            />
                        </td>
                    </template>
                    <template #className="{item, index}">
                        <td class="py-2">
                            <CFormGroup class="form-group form-row">
                                <template #input>
                                    <div class="col-sm-9">
                                        <Select2 :value="item.className"
                                                 :options="getFieldOptionsGroups()"
                                                 :settings="{theme: 'bootstrap'}"
                                                 @select="($event) => setItem(item, $event.id, index, $event.text)"
                                                 :placeholder="$t('dataset.select_data_type_placeholder')"
                                                 required
                                                 style="width: 10rem;"
                                                 theme="bootstrap"
                                        />
                                    </div>
                                </template>
                            </CFormGroup>
                        </td>
                    </template>
                    <template #formatterClassName="{item, index}">
                        <td class="py-2">
                            <CSelect
                                v-on:update:value="(value, args) => setFormatterItem(item, value, index, args)"
                                :options="getFormatterOptions()"
                                :value="item.formatterClassName"
                                :placeholder="$t('dataset.select_data_type_placeholder')"
                                class="form-control-10"
                                style="width: 100%"
                                v-c-tooltip="{content: $t('dataset.format_tooltip')}"
                            />
                        </td>
                    </template>
                    <template #languageTag="{item, index}">
                        <td class="py-2">
                            <CSelect
                                :value.sync="item.languageTag"
                                horizontal
                                class="form-control-10"
                                style="width: 100%"
                                :options="supportedLanguages"
                                required
                            />
                        </td>
                    </template>
                    <template #isPrimaryKey="{item, index}">
                        <td class="py-2">
                            <div class="form-check mx-auto checkbox-inline">
                                <input v-c-tooltip="{content: $t('dataset.invalid_reference_tooltip')}" class="form-check-input position-static" type="checkbox" v-model="item.isPrimaryKey" :checked="item.isPrimaryKey === true">
                            </div>
                        </td>
                    </template>
                    <template #isNullable="{item}">
                        <td class="py-2">
                            <div class="form-check mx-auto checkbox-inline">
                                <input v-c-tooltip="{content: $t('dataset.null_value_tooltip')}" class="form-check-input position-static" type="checkbox" v-model="item.isNullable" :checked="item.isNullable === true">
                            </div>
                        </td>
                    </template>
                    <template #isAttribute="{item}">
                        <td class="py-2">
                            <div class="form-check mx-auto checkbox-inline">
                                <input v-c-tooltip="{content: $t('dataset.attribute_tooltip')}" class="form-check-input position-static" type="checkbox" v-model="item.isAttribute" :checked="item.isAttribute === true">
                            </div>
                        </td>
                    </template>
                    <!-- TODO click on row shows data type options -->
                    <template #edit_details="{item, index}">
                        <td class="py-2">
                            <CButton v-if="showFieldButtons"
                                     :pressed.sync="item.isAlias"
                                     color="primary"
                                     variant="outline"
                                     square
                                     size="sm"
                                     class="mr-1"
                                     :disabled="disabledButtons"
                                     v-c-tooltip="{content: $t('dataset.create_alias_tooltip')}"
                            >
                              {{ $t('common.alias_button') }}
                            </CButton>
                            <CButton @click="showDetailsRow(item, index)"
                                     color="primary"
                                     variant="outline"
                                     square
                                     size="sm"
                                     class="mr-1"
                                     :disabled="disabledButtons"
                                     v-c-tooltip="{content: $t('dataset.additional_settings_tooltip')}"
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
                            <template v-if="showFieldButtons">
                                <CButton class="p-1" @click="moveFieldUp(index)" :disabled="disabledButtons"><CIcon name="cil-arrow-thick-from-bottom" /></CButton>
                                <CButton class="p-1" @click="moveFieldDown(index)" :disabled="disabledButtons"><CIcon name="cil-arrow-thick-from-top" /></CButton>
                            </template>
                        </td>
                    </template>
                </DataTable>
            </CForm>
            <CModal
                title="Delete warning"
                color="danger"
                :show.sync="dangerModal"
            >
              {{ $t('common.delete_warning_message', { item: currentItem ? currentItem.name : '' }) }}
                <template #footer>
                    <CButton @click="dangerModal = false" color="danger">{{ $t('common.discard_action') }}</CButton>
                    <CButton @click="deleteItem(currentItem, rowNumber)" color="success">{{ $t('common.accept_action') }}</CButton>
                </template>
            </CModal>
            <template v-if="$route.query.debug === 'true'">
                    <span>{{ items }}</span>
                    <span>{{ datasetFields }}</span>
            </template>
        </CCardBody>
        <CCardFooter>
            <template v-if="$route.query.type !== 'array' || ($route.query.type === 'array' && items.length === 0)">
                <CButton @click="addField" color="primary" class="mr-2" :disabled="disabledButtons">{{ $t('dataset.add_field_button') }}</CButton>
            </template>
            <CButton @click="submitForm()" color="primary" :disabled="disabledButtons">{{ $t('common.save_button') }}</CButton>
        </CCardFooter>
    </CCard>
</template>

<script>
    import { api, getComponents, processState } from "../../constants/api";
    import FieldOptions from "./FieldOptions"
    import DataTable from '../../components/table/DataTable'
    import { Translation } from '../../plugins/Translation'
    import Select2 from 'v-select2-component';
    import 'select2-bootstrap-theme/dist/select2-bootstrap.css'
    import {SUPPORTED_LOCALES} from "../../constants/globals";
    import {i18n} from "@/plugins/i18n";

    export default {
        name: "EditFields",
        components: {
          DataTable,
          FieldOptions,
          Select2
        },
        data () {
            return{
                items: [],
                datasetFields: [],
                myModal: false,
                showFieldButtons: true,
                fields: [
                    {
                        key: 'position',
                        label: '',
                        _style: 'width:1rem; min-width: 5px'
                    },
                    {
                        key: 'name',
                        label: i18n.t('common.name'),
                        _style: 'width:15rem; min-width: 150px'
                    },
                    {
                        key: 'className',
                        label: i18n.t('common.classname'),
                        _style: 'width:10rem',
                        sorter: false,
                        filter: false
                    },
                    {
                        key: 'formatterClassName',
                        label: i18n.t('common.formatter_classname'),
                        _style: 'width:10rem; min-width: 50px',
                        sorter: false,
                        filter: false
                    },
                    {
                        key: 'languageTag',
                        label: i18n.t('common.locale'),
                        _style: 'width:10rem; min-width: 120px',
                        sorter: false,
                        filter: false
                    },
                    {
                        key: 'isPrimaryKey',
                        label: i18n.t('common.primary'),
                        _style: 'width:20px',
                        sorter: false,
                        filter: false
                    },
                    {
                        key: 'isNullable',
                        label: i18n.t('common.nullable'),
                        _style: 'width:20px',
                        sorter: false,
                        filter: false
                    },
                    {
                        key: 'edit_details',
                        label: i18n.t('common.actions'),
                        _style: 'width:20%',
                        sorter: false,
                        filter: false
                    }
                ],
                perPage: 5,
                componentsData: {sinks: [], generators: [], fields: [], formatters: []},
                fieldConfigs: [],
                fieldOptions: [],
                formatterOptions: [],
                dataset: null,
                currentItem: null,
                parent: null,
                name: '',
                datasetName: '',
                rowNumber: -1,
                showDetails: false,
                isLoading: true,
                disabledButtons: false,
                dangerModal: false,
                init: false,
                supportedLanguages: SUPPORTED_LOCALES,
                errorMessage: '',
                errorDetails: '',
                displayError: false
            }
        },
        mounted() {
            getComponents()
            .then(response => {
                this.$set(this.componentsData, "fields", response.fields)
                this.$set(this.componentsData, "formatters", response.formatters)
                this.loadItems()
            })
            .catch(err => {})
            if (this.$route.query.name && !this.$route.query.type) {
                this.datasetName = this.$route.query.name
                sessionStorage.setItem("dataset", this.datasetName)
            } else {
                this.datasetName = sessionStorage.getItem("dataset")
            }
        },
        methods: {
            loadItems() {
                this.showFieldButtons = !(this.$route.query.type && this.$route.query.type === 'array')

                processState(this, "beforeSubmit")

                if (this.$route.query.type) {
                    this.getNestedFields(this.$route.params.fieldId, this.$route.query.type)
                }

                api.get(`/dataset/${this.$route.params.id}/fields?includeNested=true`)
                    .then(response => {
                        if (response.data && response.data.payload.length > 0) {
                            this.datasetFields = response.data.payload
                            if (this.$route.query.type) {
                                this.parent = this.datasetFields.find(df => this.$route.params.fieldId === df.externalId)
                            } else {
                                this.items = [...this.datasetFields].filter(i=>!i.isNested)
                                this.items.forEach(i => {
                                    i.isUpdate = true
                                    i.datasetId = this.$route.params.id
                                })
                                this.parent = {isNested: false, name: 'FIX IT'}
                            }
                        }
                        this.$workspace = response.data.workspaceId
                        this.isLoading = false
                        this.init = true
                        processState(this, "onSuccess")
                    })
                    .catch(err => processState(this, "onError", err))
            },
            addToConfig(key, value) {
                this.items[this.currentItem].config[key] = value
                this.items.push()
            },
            updateDatasetFields() {
                // find position
                this.items.forEach(item => {
                    const index = this.datasetFields.findIndex(df => item.externalId ? df.externalId === item.externalId : false)

                    if (index > -1) {
                        this.datasetFields[index] = item
                    } else {
                        this.datasetFields.push(item)
                    }
                })
            },
            redirectToNestedField(item) {
                console.log("redirect: " + item.name)
                this.showDetails = false
            },
            getNestedFields(fieldId, type) {
                api.get(`/field/${fieldId}/${type}`)
                .then(response => {
                    if (response.data && response.data.payload) {
                        this.items = response.data.payload
                    }
                    this.items.forEach(i => {
                        i.isUpdate = true
                        i.datasetId = this.$route.params.id
                    })
                    //this.$workspace = response.data.workspaceId
                })
                .catch(err => {
                    processState(this, "onError", err)
                })
            },
            showDetailsRow(item, index) {
                this.showDetails = true
                this.myModal = false
                this.rowNumber = index
                this.currentItem = index
                let fieldAndFormatterOptions = []

                const found = this.componentsData.fields.find(field => field.className === item.className)

                if (found) {
                    fieldAndFormatterOptions = this.getOptionsFromConfig(found)
                }

                const formatterFound = this.componentsData.formatters.find(field => field.className === item.formatterClassName)

                if (formatterFound) {
                    //fieldAndFormatterOptions = this.getOptionsFromConfig(formatterFound)
                    this.formatterOptions = this.getOptionsFromConfig(formatterFound)
                }

                this.fieldOptions = fieldAndFormatterOptions
            },
            getOptionsFromConfig(componentFound) {
                for (let input of componentFound.configProperties) {
                    if (input.key in this.items[this.currentItem].config) {
                        input.value = this.items[this.currentItem].config[input.key]
                    } else {
                        input.value = input.defaultValue
                        // this.items[this.currentItem].config[input.key] = input.defaultValue
                        if (input.type === 'LIST') {
                            this.items[this.currentItem].config[input.key] = []
                        }
                    }
                    input.possibleValues =  input.possibleValues.sort()
                }
                return componentFound.configProperties
            },
            showDeleteWarning(item, index) {
                this.showDetails = false
                this.dangerModal = true
                this.currentItem = item
                this.rowNumber = index
            },
            setItem(item, value, index, args) {
                if (item.className !== value) {
                    this.$set(item, 'config', {})
                }
                if (!item.name) {
                    //item.name = args.target.selectedOptions[0].label
                    item.name = args
                }
                item.className = value

                if (item.isNested && !item.externalId) {
                    processState(this, "beforeSubmit")

                    api.post("/field", item)
                        .then(response => {
                            if (response.data) {
                                item.externalId = response.data.externalId
                                item.isUpdate = true
                            }
                            this.showDetailsRow(item, index)
                            processState(this, "onSuccess")
                        }).catch(err => {
                        processState(this, "onError", err)
                    })
                } else {
                    this.showDetailsRow(item, index)
                }
            },
            setFormatterItem(item, value, index, args) {
                item.formatterClassName = value === 'None' ? '' : value
                this.showDetailsRow(item, index)
            },
            moveFieldUp(index) {
                this.showDetails = false
                if (index >= 0) {
                    if (index === 0 || index + 1 === this.items.length) {
                        let topItem = this.items.shift()
                        this.items.push(topItem)
                    } else {
                        let item = this.items[index]
                        this.items[index] = this.items[index - 1]
                        this.items[index - 1] = item
                        this.items.push()
                    }
                }
                this.recalculatePositions()
            },
            moveFieldDown(index) {
                this.showDetails = false
                if (index >= 0) {
                    if (index === 0 || index + 1 === this.items.length) {
                        let bottomItem = this.items.pop()
                        this.items.unshift(bottomItem)
                    } else {
                        let item = this.items[index]
                        this.items[index] = this.items[index + 1]
                        this.items[index + 1] = item
                        this.items.push()
                    }
                }
                this.recalculatePositions()
            },
            recalculatePositions() {
                for (let i=0; i<this.items.length; i++) {
                    this.items[i].position = i + 1;
                }
            },
            addField() {
                this.showDetails = false
                this.items.push({
                    name: "",
                    datasetId: this.$route.params.id,
                    languageTag: Translation.getUserLang().lang,
                    isPrimaryKey: false,
                    className: "",
                    formatterClassName: "",
                    isNullable: false,
                    config: {},
                    isNested: !!this.$route.query.type,
                    isUpdate: false,
                    position: this.items.length + 1,
                    isAlias: false,
                    isAttribute: false
                })
                this.init = true
            },
            rowClicked(item, index) {
                this.rowNumber = index
                this.showDetails = !this.showDetails
                this.showDetailsRow(item, index)
            },
            deleteItem(item, index) {
                processState(this, "beforeSubmit")
                // TODO if array or complex: remove from parent config
                if (item.isUpdate) {
                    api.delete("/field/" + item.externalId)
                    .then(response => {
                        this.items.splice(index, 1)
                        const dfIndex = this.datasetFields.findIndex(df => df.externalId === item.externalId)
                        if (dfIndex > -1) {
                            this.datasetFields.splice(dfIndex, 1)
                        }
                        processState(this, "onSuccess")

                    }).catch(err => {
                        processState(this, "onError", err)
                    })
                } else {
                    processState(this, "onSuccess")

                    this.items.splice(index, 1)
                    const dfIndex = this.datasetFields.findIndex(df => df.externalId === item.externalId)
                    if (dfIndex > -1) {
                        this.datasetFields.splice(dfIndex, 1)
                    }
                }
            },
            submitForm(redirectItem, direction) {
                processState(this, "beforeSubmit")
                this.updateDatasetFields()
                this.showDetails = false

                let forms = this.$el.querySelectorAll('form')
                forms.forEach(form => {
                  if (!form.checkValidity()) {
                    form.classList.add('was-validated')
                    processState(this, "invalidForm")
                    //return false
                  }
                })

                let copyItems = [...this.items]
                if (this.$route.query.type === 'array') {
                    if (this.items.length > 0) {
                        this.parent.config['field.array.element'] = this.items[0].externalId
                    } else {
                        this.parent.config['field.array.element'] = null
                    }
                    copyItems.push(this.parent)
                } else if (this.$route.query.type === 'complex') {
                    this.parent.config['field.complex.values'] = this.items.map(i => i.externalId)
                    copyItems.push(this.parent)
                }

                api.post("/field/batch", {
                    "datasetId": this.$route.params.id,
                    "fields": copyItems
                })
                .then(response => {
                    this.$eventHub.$emit('show-toast', "Saved successfully");
                    for (let index=0; index<response.data.payload.length; index++) {
                        let item = copyItems[index]
                        item.externalId = response.data.payload[index].externalId
                        item.isUpdate = true
                    }

                    if (redirectItem){

                        if (redirectItem === this.parent && !redirectItem.isNested) {
                            this.$router.push({
                                path: `/datasets/fields/${this.$route.params.id}`,
                                query: {name: redirectItem.name}
                            })
                        } else {
                            if (direction === 'top') {
                                redirectItem = this.datasetFields.find(df => {
                                    if ('field.complex.values' in df.config) {
                                        return df.config['field.complex.values'].includes(redirectItem.externalId)
                                    } else if ('field.array.element' in df.config) {
                                        return df.config['field.array.element'] === redirectItem.externalId
                                    }
                                    return false
                                })
                            }
                            this.$router.push({
                                path: `/datasets/fields/${this.$route.params.id}/${redirectItem.externalId}`,
                                query: {
                                    name: redirectItem.name,
                                    type: redirectItem.className.endsWith('ComplexField') ? 'complex' : 'array',
                                }
                            })
                        }
                    } else {
                        processState(this, "onSuccess")
                    }
                }).catch(err => {
                    processState(this, "onError", err)
                })

            },
            getFormatterOptions() {
                let options = []

                //options.push({value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
                for (let formatter of this.componentsData.formatters) {
                    //options.push(field.name)
                    options.push({label: formatter.name, value:formatter.className, config: formatter.configProperties })
                }
                options.sort(function(a, b){
                    if ( a.label < b.label ){
                        return -1;
                    }
                    if ( a.label > b.label ){
                        return 1;
                    }
                    return 0;
                });
                options.unshift({value: "None", label: "None", config: [], selected: "selected"})
                return options
            },
            getFieldOptionsGroups() {
                let options = []
                let groups = {}
                for (let field of this.componentsData.fields) {
                    if (!(field.grouping in groups)) {
                        groups[field.grouping] = []
                    }
                    groups[field.grouping].push({id: field.className, text: field.name})
                }
                for (let group in groups) {
                    groups[group].sort(function(a, b){
                        if ( a.text < b.text ){
                            return -1
                        }
                        if ( a.text > b.text ){
                            return 1
                        }
                        return 0
                    })
                    options.push({"children": groups[group], "text": group.toUpperCase()})
                }
                options.sort(function(a, b){
                    if ( a.text < b.text ){
                        return -1
                    }
                    if ( a.text > b.text ){
                        return 1
                    }
                    return 0
                })
                options.unshift({id: null, text: "Select one...", disabled: "disabled", selected: "selected"})
                return options
            },
            getFieldOptions() {
                let options = []

                //options.push({value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
                for (let field of this.componentsData.fields) {
                    //options.push(field.name)
                    options.push({label: field.name, value:field.className, config: field.configProperties })
                }
                options.sort(function(a, b){
                    if ( a.label < b.label ){
                        return -1;
                    }
                    if ( a.label > b.label ){
                        return 1;
                    }
                    return 0;
                });
                options.unshift({value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
                return options
            }
        },
        watch:{
            '$route' (to, from){
                this.loadItems()
            }
        }
    }
</script>

<style scoped>
    input[type="checkbox"] {
        margin-left: auto;
    }
</style>