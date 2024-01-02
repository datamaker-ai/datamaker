<template>
    <CRow>
        <CCol col="12" lg="6">
            <CCard>
                <CCardHeader>
                  {{ $t('sinks.edit_header') }}
                </CCardHeader>
                <CCardBody>
                    <CAlert :show="displayError" color="danger">
                      <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
                      <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
                    </CAlert>
                    <CForm id="sink">
                        <CInput
                                :label="$t('common.name')"
                                :placeholder="$t('common.name_placeholder')"
                                v-model="sink.name"
                                horizontal
                                required
                        />
                        <CSelect
                                :value.sync="sink.workspaceId"
                                :options="workspaces"
                                :label="$t('common.workspace_label')"
                                horizontal
                                :placeholder="$t('common.workspace_title')"
                                :title="$t('common.workspace_title')"
                                required
                        />
                        <CFormGroup class="form-group form-row">
                            <template #label>
                                <label :for="safeId()" class="col-form-label col-sm-3">
                                  {{ $t('sinks.select_sink_label') }}
                                </label>
                            </template>
                            <template #input>
                                <div class="col-sm-9">
                                    <Select2 v-model="sink.sinkClassName"
                                             :options="getSinkOptionsGroups()"
                                             :settings="{theme: 'bootstrap'}"
                                             @change="sinkSelected($event)"
                                             :placeholder="$t('sinks.select_sink_placeholder')"
                                             required
                                             theme="bootstrap"
                                    />
                                </div>
                            </template>
                        </CFormGroup>
                        <template v-for="sinkConfig in sinkConfigs">
                            <CFormGroup
                                    class="form-group form-row"
                                    v-if="sinkConfig.type === 'BOOLEAN'"
                            >
                                <template #label>
                                    <label :for="safeId()" class="col-form-label col-sm-3">
                                        {{ sinkConfig.description }}
                                    </label>
                                </template>
                                <template #input>
                                    <CSwitch class="mx-1"
                                             :id="safeId()"
                                             color="primary"
                                             :value.sync="sinkConfig.value"
                                             :checked="sinkConfig.value === true"
                                             variant="3d"
                                             label-on="on"
                                             label-off="off"
                                             @update:checked="addToConfig(sinkConfig.key, $event)"/>
                                </template>
                            </CFormGroup>
                            <CInput v-if="sinkConfig.type === 'PASSWORD'"
                                    v-bind:key="sinkConfig.key"
                                    v-bind:label="sinkConfig.description"
                                    v-bind:placeholder="sinkConfig.description"
                                    v-bind:value="displaySpecialCharacters(sinkConfig.value)"
                                    @update:value="addToConfig(sinkConfig.key, $event)"
                                    type="password"
                                    horizontal
                            />
                            <!-- TODO insert icon for expression -->
                            <CInput v-if="(sinkConfig.type === 'STRING' || sinkConfig.type === 'EXPRESSION') && sinkConfig.possibleValues.length === 0"
                                    v-bind:key="sinkConfig.key"
                                    v-bind:label="sinkConfig.description"
                                    v-bind:placeholder="sinkConfig.description"
                                    v-bind:value="displaySpecialCharacters(sinkConfig.value)"
                                    @update:value="addToConfig(sinkConfig.key, $event)"
                                    type="text"
                                    horizontal
                            >
                                <template #prepend-content>
                                    <CIcon name="cil-code"
                                           v-c-tooltip="{content: $t('common.support_expression_tooltip')}"
                                           v-if="sinkConfig.type === 'EXPRESSION'"/>
                                </template>
                            </CInput>
                            <CSelect v-if="sinkConfig.type === 'STRING' && sinkConfig.possibleValues.length > 0"
                                     v-bind:label="sinkConfig.description"
                                     :options="sinkConfig.possibleValues"
                                     v-bind:value="sinkConfig.value"
                                     @update:value="addToConfig(sinkConfig.key, $event)"
                                     placeholder="Please select one"
                                     title="Select a value"
                                     horizontal
                            />
                            <CInput v-if="sinkConfig.type === 'DATE'"
                                    v-bind:key="sinkConfig.key"
                                    v-bind:label="sinkConfig.description"
                                    v-bind:placeholder="sinkConfig.description"
                                    v-bind:value="sinkConfig.value"
                                    @update:value="addToConfig(sinkConfig.key, $event)"
                                    type="datetime-local"
                                    horizontal
                            />
                            <CInput v-if="sinkConfig.type === 'NUMERIC'"
                                    v-bind:key="sinkConfig.key"
                                    v-bind:label="sinkConfig.description"
                                    v-bind:placeholder="sinkConfig.description"
                                    v-bind:value="sinkConfig.value"
                                    @update:value="addToConfig(sinkConfig.key, $event)"
                                    type="number"
                                    step="any"
                                    horizontal
                            />
                        </template>
                    </CForm>
                </CCardBody>
                <CCardFooter>
                    <CButton type="submit" size="sm" color="primary" @click="submitForm" class="mr-1" :disabled="disabledButtons">
                        <CIcon name="cil-check-circle"/>
                      {{ $t('common.submit_button') }}
                    </CButton>
                </CCardFooter>
                <template v-if="isLoading">
                    <CElementCover
                            :boundaries="[
                                    { sides: ['top'], query: 'header' },
                                    { sides: ['bottom'], query: 'footer' }
                                ]"
                    />
                </template>
            </CCard>
        </CCol>
    </CRow>
</template>

<script>
    import {api, getComponents, getWorkspaceOptions, processState} from "../../constants/api";
    import makeUid from "@coreui/utils/src/make-uid";
    import Select2 from 'v-select2-component';
    import 'select2-bootstrap-theme/dist/select2-bootstrap.css'

    export default {
        name: "EditSinkConfiguration",
        components: {
            Select2
        },
        data() {
            return {
                componentsData: {sinks: []},
                sinkConfigs: [],
                selectedSink: null,
                showSelect: true,
                name: '',
                workspace: null,
                workspaces: [],
                config: {},
                errorMessage: '',
                errorDetails: '',
                disabledButtons: false,
                displayError: false,
                previousValue: '',
                isLoading: true,
                sink: {
                    name: '',
                    config: {},
                    sinkClassName: null,
                    workspaceId: null
                }
            }
        },
        mounted() {
            let components = getComponents()
                .then(response => {
                    this.$set(this.componentsData, "sinks", response.sinks)
                })
                .catch(err => {
                })
            let workspaces = getWorkspaceOptions()
                .then(value => {
                    this.workspaces = value
                })
                .catch(err => {
                })
            Promise.all([components, workspaces]).then((values) => {
                if (this.$route.params.id) {
                    processState(this, "beforeSubmit")
                    let id = this.$route.params.id
                    api.get("/sinks/" + id)
                        .then(response => {
                            this.sink = response.data.payload
                            this.previousValue = this.sink.sinkClassName
                            this.sinkSelected(this.sink.sinkClassName)
                            processState(this, "onSuccess")
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        })
                } else {
                  this.isLoading = false
                }
            })
        },
        updated() {
        },
        methods: {
            addToConfig(key, value) {
                this.$set(this.sink.config, key, value)
            },
            escapeSpecialCharacters(value) {
                return value.replace('\n', '\\n')
            },
            getSinkOptionsGroups() {
                let options = []
                let groups = {}
                for (let sink of this.componentsData.sinks) {
                    if (!(sink.grouping in groups)) {
                        groups[sink.grouping] = []
                    }
                    groups[sink.grouping].push({id: sink.className, text: sink.name})
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
            getSinkOptions() {
                let options = []

                for (let sink of this.componentsData.sinks) {
                    options.push({label: sink.name, value: sink.className, config: sink.configProperties})
                }
                options.sort(function(a, b){
                    if ( a.label < b.label ){
                        return -1
                    }
                    if ( a.label > b.label ){
                        return 1
                    }
                    return 0
                })
                options.unshift({value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
                return options
            },
            displaySpecialCharacters(value) {
                return value ? value.replace('\n', '\\n') : ''
            },
            safeId() {
                if (this.id || this.$attrs.id) {
                    return this.id || this.$attrs.id
                }
                return makeUid()
            },
            sinkSelected (item, index) {
                let configs = []
                let sink = this.getSinkOptions().find(element => element.value === item)

                if (this.previousValue !== item) {
                    this.sink.config = {}
                }
                this.previousValue = item
                // if (sink.value !== this.sink.sinkClassName || this.sink.config == null) {
                //     this.sink.config = {}
                // }
                for (let input of sink.config) {
                    if (this.sink.config[input.key]) {
                        input.value = this.sink.config[input.key]
                    } else {
                        input.value = input.defaultValue === null ? "" : input.defaultValue.toString()
                    }
                    configs.push(input)
                }
                this.sink.sinkClassName = sink.value
                this.sinkConfigs = configs
            },
            resetForm: function (event) {
                let form = this.$el.querySelector('form')
                form.reset()
            },
            submitForm: function (event) {
                processState(this, "beforeSubmit")

                let form = this.$el.querySelector('form')
                if (!form.checkValidity()) {
                    form.classList.add('was-validated')
                    processState(this, "invalidForm")
                    return false
                }

                if (this.$route.params.id) {
                    let id = this.$route.params.id
                    api.put('/sinks/' + id, this.sink)
                        .then(response => {
                            processState(this, "onSuccess")
                            this.$router.push('/sinks')
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        })
                } else {
                    api.post('/sinks', this.sink)
                        .then(response => {
                            processState(this, "onSuccess")
                            this.$router.push('/sinks')
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        })
                }
            }
        }
    }
</script>

<style scoped>

</style>