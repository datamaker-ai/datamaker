<template>
    <CContainer :fluid="true">
        <CRow>
            <CCol lg="8">
                <CCard>
                    <CCardHeader>
                      {{ $t('jobs.create_title') }}
                    </CCardHeader>
                    <CCardBody>
                        <CAlert :show="displayError" color="danger">
                          <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
                          <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
                        </CAlert>
                        <CTabs :active-tab.sync="currentTab" @update:activeTab="processTab">

                            <CTab title="Settings" :disabled="!tab1Valid">
                                <template slot="title">
                                    {{ $t('jobs.settings_tab_title') }} <CBadge color="danger" v-if="tab1Errors > 0">&nbsp;!&nbsp;</CBadge>
                                </template>
                                <div class="mt-2">
                                    <CForm id="form-tab1" class="needs-validation" novalidate>
                                        <CSelect
                                            :options="workspaces"
                                            :value.sync="dataJob.workspaceId"
                                            :label="$t('common.workspace_label')"
                                            horizontal
                                            :title="$t('common.workspace_title')"
                                            required
                                            :invalid-feedback="$t('common.workspace_title')"
                                        />
                                        <CInput
                                            :description="$t('jobs.name_description')"
                                            :label="$t('jobs.name_label')"
                                            horizontal
                                            :placeholder="$t('jobs.name_placeholder')"
                                            v-model="dataJob.name"
                                            required
                                        />
                                        <CInput
                                            :description="$t('jobs.description_description')"
                                            :label="$t('jobs.description_label')"
                                            horizontal
                                            :placeholder="$t('jobs.description_placeholder')"
                                            v-model="dataJob.description"
                                        />
                                        <CSelect
                                            :description="$t('jobs.cron_description')"
                                            :label="$t('jobs.cron_label')"
                                            :options="['Cron', 'Once', 'Random']"
                                            :value.sync="scheduleType"
                                            required
                                            horizontal
                                        />
                                        <template v-if="scheduleType === 'Cron'">
                                            <!--
                                            <div class="card-header-actions">
                                                <a
                                                    href="https://www.manpagez.com/man/5/crontab/"
                                                    class="card-header-action"
                                                    rel="noreferrer noopener"
                                                    target="_blank"
                                                >
                                                    <small class="text-muted">docs</small>
                                                </a>
                                            </div>
                                            -->
                                            <!-- 0 * * * * * -->
                                            <CInput
                                                :description="$t('jobs.cron_expression_description')"
                                                :label="$t('jobs.cron_expression_label')"
                                                horizontal
                                                :placeholder="$t('jobs.cron_expression_placeholder')"
                                                :value.sync="dataJob.schedule"
                                            />
                                        </template>
                                        <template v-if="scheduleType === 'Random'">
                                            <CInput
                                                type="range"
                                                :label="$t('jobs.cron_min_delay_label', {minDelay: minDelay})"
                                                v-model="minDelay"
                                                min="0" max="86400"
                                                horizontal
                                                custom
                                            />
                                            <CInput
                                                type="range"
                                                :label="$t('jobs.cron_max_delay_label', {maxDelay: maxDelay})"
                                                v-model="maxDelay"
                                                min="0" max="86400"
                                                horizontal
                                                custom
                                            />
                                            <input
                                                type="hidden"
                                                :value="dataJob.schedule = 'random ' + minDelay + ' ' + maxDelay"
                                            />
                                        </template>
                                        <CInput
                                            :description="$t('jobs.max_records_description')"
                                            :label="$t('jobs.max_records_label')"
                                            horizontal
                                            :placeholder="$t('jobs.max_records_placeholder')"
                                            v-model="dataJob.numberOfRecords"
                                            min="1"
                                            :invalid-feedback="$t('jobs.max_records_invalid_feedback')"
                                            type="number"
                                        />
                                        <CFormGroup class="form-group form-row">
                                            <template #label>
                                                <label for="randomRecords" class="col-form-label col-sm-3">
                                                  {{ $t('jobs.random_records_label') }}
                                                </label>
                                            </template>
                                            <template #input>
                                                <CSwitch class="mx-1"
                                                         id="randomRecords"
                                                         color="primary"
                                                         :value="dataJob.randomizeNumberOfRecords"
                                                         :checked="dataJob.randomizeNumberOfRecords === true"
                                                         variant="3d"
                                                         label-on="on"
                                                         label-off="off"
                                                         @update:checked="dataJob.randomizeNumberOfRecords = $event" />
                                            </template>
                                        </CFormGroup>
                                        <CFormGroup class="form-group form-row">
                                            <template #label>
                                                <label for="flushRecord" class="col-form-label col-sm-3">
                                                  {{ $t('jobs.flush_sink_label') }}
                                                </label>
                                            </template>
                                            <template #input>
                                                <CSwitch class="mx-1"
                                                         id="flushRecord"
                                                         color="primary"
                                                         :value="dataJob.flushOnEveryRecord"
                                                         @update:checked="dataJob.flushOnEveryRecord = $event"
                                                         :checked="dataJob.flushOnEveryRecord === true"
                                                         variant="3d"
                                                         label-on="ON"
                                                         label-off="OFF" />
                                            </template>
                                        </CFormGroup>
                                        <CFormGroup class="form-group form-row">
                                            <template #label>
                                                <label for="useBuffer" class="col-form-label col-sm-3">
                                                  {{ $t('jobs.use_buffer_label') }}
                                                </label>
                                            </template>
                                            <template #input>
                                                <CSwitch class="mx-1"
                                                         id="useBuffer"
                                                         color="primary"
                                                         :value="dataJob.useBuffer"
                                                         :checked="dataJob.useBuffer === true"
                                                         variant="3d"
                                                         label-on="on"
                                                         label-off="off"
                                                         @update:checked="dataJob.useBuffer = $event" />
                                            </template>
                                        </CFormGroup>
                                        <CInput
                                            :description="$t('jobs.buffer_size_description')"
                                            :label="$t('jobs.buffer_size_label')"
                                            horizontal
                                            :placeholder="$t('jobs.buffer_size_placeholder')"
                                            v-model="dataJob.bufferSize"
                                            type="number"
                                        />
                                      <CInput
                                          :description="$t('jobs.thread_pool_size_description')"
                                          :label="$t('jobs.thread_pool_size_label')"
                                          horizontal
                                          :placeholder="$t('jobs.thread_pool_size_placeholder')"
                                          v-model="dataJob.threadPoolSize"
                                          type="number"
                                      />
                                      <CFormGroup class="form-group form-row">
                                        <template #label>
                                          <label for="isReplayable" class="col-form-label col-sm-3">
                                            {{ $t('jobs.replayable_label') }}
                                          </label>
                                        </template>
                                        <template #input>
                                          <CSwitch class="mx-1"
                                                   id="isReplayable"
                                                   color="primary"
                                                   :value="dataJob.replayable"
                                                   :checked="dataJob.replayable === true"
                                                   variant="3d"
                                                   label-on="on"
                                                   label-off="off"
                                                   @update:checked="dataJob.replayable = $event"
                                                   v-c-tooltip="{content: $t('dataset.replayable_tooltip')}"
                                          />
                                        </template>
                                      </CFormGroup>
                                      <CInput
                                          :description="$t('jobs.replay_history_size_description')"
                                          :label="$t('jobs.replay_history_size_label')"
                                          horizontal
                                          :placeholder="$t('jobs.replay_history_size_placeholder')"
                                          v-model="dataJob.replayHistorySize"
                                          type="number"
                                          min="1"
                                      />
                                    </CForm>
                                </div>
                            </CTab>
                            <CTab title="Datasets" :disabled="!tab2Valid">
                                <template slot="title">
                                  {{ $t('jobs.datasets_tab_title') }} <CBadge color="danger" v-if="tab2Errors > 0">&nbsp;!&nbsp;</CBadge>
                                </template>
                                <div class="mt-2">
                                    <CForm id="form-tab2" class="">
                                        <CFormGroup class="form-check">
                                            <template #input>
                                                <input type="checkbox" @click="selectAllDatasets" v-model="allSelected" class="form-check-input">
                                                <label class="form-check-label">
                                                  {{ $t('jobs.all_datasets_label') }}
                                                </label>
                                            </template>
                                        </CFormGroup>
                                        <!--
                                        TODO automatically add reference field based on dataset
                                        -->
                                        <template v-for="dataset in workspaceDatasets">
                                            <CFormGroup class="form-check">
                                                <template #input>
                                                    <input
                                                        type="checkbox"
                                                        name="datasets"
                                                        :value="dataset.externalId"
                                                        v-bind:value="dataset.externalId"
                                                        v-model="dataJob.datasets"
                                                        class="form-check-input"
                                                        @change="updateCheckall()"
                                                    >
                                                    <label class="form-check-label">
                                                        {{ dataset.name + (dataset.description ? ' (' + dataset.description + ')' : '') }}
                                                    </label>
                                                </template>
                                            </CFormGroup>
                                        </template>
                                    </CForm>
                                </div>
                            </CTab>
                            <CTab title="Generator" :disabled="!tab3Valid">
                                <template slot="title">
                                  {{ $t('jobs.generator_tab_title') }} <CBadge color="danger" v-if="tab3Errors > 0">&nbsp;!&nbsp;</CBadge>
                                </template>
                                <div class="mt-2">
                                    <CForm id="form-tab3" class="">
                                        <CSelect
                                            :label="$t('jobs.generator_label')"
                                            :options="getGeneratorOptions()"
                                            :placeholder="$t('jobs.generator_placeholder')"
                                            :title="$t('jobs.generator_title')"
                                            :value="dataJob.generator"
                                            @update:value="generatorSelected"
                                            required
                                        />
                                        <template v-if="generatorConfigs.length > 0">
                                            <span><strong>{{ $t('common.field_options') }}</strong></span>
                                            <FieldOptions :item="{className: ''}"
                                                          :current-item="0"
                                                          :items="[{config:{}}]"
                                                          :fieldOptions="generatorConfigs"
                                                          @config-change="(key, value) => addToGeneratorConfig(key, value)">
                                            </FieldOptions>
                                        </template>
                                    </CForm>
                                </div>
                            </CTab>
                            <CTab title="Sinks" :disabled="!tab4Valid">
                                <template slot="title">
                                  {{ $t('jobs.sinks_tab_title') }} <CBadge color="danger" v-if="tab4Errors > 0">&nbsp;!&nbsp;</CBadge>
                                </template>
                                <div class="mt-2">
                                    <CForm id="form-tab4" class="">
                                        <CFormGroup class="form-group form-row">
                                            <template #label>
                                                <label :for="safeId()" class="col-form-label col-sm-3">
                                                  {{ $t('jobs.sink_label') }}
                                                </label>
                                            </template>
                                            <template #input>
                                                <div class="col-sm-9">
                                                    <Select2 v-model="dataJob.sinks[0]"
                                                             :options="getSinkOptionsGroups()"
                                                             :settings="{theme: 'bootstrap'}"
                                                             @change="sinkSelected($event)"
                                                             :placeholder="$t('jobs.sink_placeholder')"
                                                             required
                                                             theme="bootstrap"
                                                    />
                                                </div>
                                            </template>
                                        </CFormGroup>
                                        <CSelect
                                            :label="$t('jobs.global_sink_label')"
                                            :options="getGlobalSinkOptions()"
                                            :placeholder="$t('jobs.global_sink_placeholder')"
                                            :title="$t('jobs.global_sink_title')"
                                            horizontal
                                            @update:value="(item) => globalSinkSelect(item)"
                                        />
                                        <template v-if="sinkConfigs.length > 0">
                                            <span><strong>{{ $t('common.sink_options') }}</strong></span>
                                            <FieldOptions :item="{className: ''}"
                                                          :current-item="0"
                                                          :items="[{config: this.dataJob.config[this.dataJob.sinks[0]]}]"
                                                          :fieldOptions="sinkConfigs"
                                                          @config-change="(key, value) => addToSinkConfig(key, value)">
                                            </FieldOptions>
                                        </template>
                                    </CForm>
                                </div>
                            </CTab>
                            <CTab :title="$t('jobs.finish_tab_title')" :disabled="!tab1Valid || !tab2Valid || !tab3Valid || !tab4Valid">
                                <div class="mt-2">
                                    <CInput
                                        :label="$t('jobs.schedule_label')"
                                        horizontal
                                        v-model="dataJob.schedule"
                                        disabled="disabled"
                                    />
                                    <!--  Validate Fields not empty, generator not null and sinks not empty  -->
                                    <CButton @click="runJob" class="mr-1" color="primary">{{ $t('jobs.schedule_job_button') }}</CButton>
                                    <CButton @click="getFile" class="mr-1" color="primary">{{ $t('jobs.download_file_button') }}</CButton>
                                    <CButton @click="getOutput" class="mr-1" color="primary">{{ $t('jobs.show_output_button') }}</CButton>
                                </div>
                            </CTab>
                        </CTabs>
                        <template v-if="isLoading">
                            <CElementCover
                                :boundaries="[
                                    { sides: ['top'], query: 'header' },
                                    { sides: ['bottom'], query: 'footer' }
                                ]"
                            />
                        </template>
                    </CCardBody>
                    <CCardFooter>
                        <CButton class="mr-1" color="primary" v-bind:to="{path:'/jobs'}">{{ $t('common.cancel_button') }}</CButton>
                        <CButton class="mr-1" color="primary" @click="processPreviousTab" :disabled="currentTab === 0">{{ $t('common.previous_button') }}</CButton>
                        <CButton class="mr-1" color="primary" @click="processNextTab" :disabled="currentTab === 4">{{ $t('common.next_button') }}</CButton>
                    </CCardFooter>
                </CCard>
            </CCol>
        </CRow>
        <CModal size="xl"
                title="Content"
                color="info"
                :show.sync="showContent"
        >
            {{ content }}
        </CModal>
        <template v-if="$route.query.debug === 'true'">
            <CRow>
                Schedule: {{ scheduleType }}
                {{ dataJob }}
            </CRow>
        </template>
    </CContainer>
</template>

<script>
    import {api, endpoint, getComponents, getWorkspaceOptions, processState} from "../../constants/api";
    import makeUid from "@coreui/utils/src/make-uid";
    import FieldOptions from "../datasets/FieldOptions"
    import Select2 from 'v-select2-component';
    import 'select2-bootstrap-theme/dist/select2-bootstrap.css'

    export default {
        name: "EditGenerateDataJob",
        components: {
            FieldOptions,
            Select2
        },
        data () {
            return{
                content: '',
                isUpdate: false,
                isLoading: false,
                workspaceDatasets: [],
                allSelected: false,
                componentsData: {sinks: [], generators: []},
                generatorConfigs: [],
                globalSinks:[],
                sinkConfigs: [],
                workspaces: [],
                currentTab: 0,
                previousTab: -1,
                tab1Errors: 0,
                tab2Errors: 0,
                tab3Errors: 0,
                tab4Errors: 0,
                tab1Valid: false,
                tab2Valid: false,
                tab3Valid: false,
                tab4Valid: false,
                displayError: false,
                errorMessage: null,
                errorDetails: null,
                showContent: false,
                dataJob: {
                    name: '',
                    description: '',
                    workspace: '',
                    numberOfRecords: 10,
                    schedule: 'once',
                    workspaceId: '',
                    datasets: [],
                    generator: '',
                    sinks: [],
                    bufferSize: 1024,
                    threadPoolSize: 10,
                    useBuffer: false,
                    flushOnEveryRecord: false,
                    randomizeNumberOfRecords: false,
                    replayable: false,
                    replayHistorySize: 10,
                    config: {}
                },
                scheduleType: 'Once',
                minDelay: 10,
                maxDelay: 60
            }
        },
        mounted() {
            this.isUpdate = !!this.$route.params.id
            // if (this.isUpdate) {
            //     this.tab1Valid = true
            //     this.tab2Valid = true
            //     this.tab3Valid = true
            //     this.tab4Valid = true
            // }
            this.isLoading = true

          // /{workspaceId}/datasets
          //   api.get(`/workspace/${this.dataJob.workspaceId}/datasets`)
          //   .then(response => {
          //       this.workspaceDatasets = response.data.payload
          //   })
          //   .catch(err => {})

            getComponents()
            .then(response => {
                this.$set(this.componentsData, "sinks", response.sinks)
                this.$set(this.componentsData, "generators", response.generators)
                if (this.isUpdate) {
                    api.get(`/generate-data-job/${this.$route.params.id}`)
                    .then(response => {
                        this.dataJob = response.data.payload
                        this.isLoading = false
                        if (this.dataJob.schedule === 'once') {
                            this.scheduleType = 'Once'
                        } else if (this.dataJob.schedule.startsWith('random')) {
                            this.scheduleType = 'Random'
                        } else {
                            this.scheduleType = 'Cron'
                        }
                        api.get(`/sinks?workspaceId=${this.dataJob.workspaceId}`)
                            .then(response => {
                                this.globalSinks = response.data.payload
                            })
                            .catch(err => {})
                        this.allSelected = this.workspaceDatasets.length === this.dataJob.datasets.length
                        this.generatorSelected(this.dataJob.generator)
                        this.sinkSelected(this.dataJob.sinks[0])
                    })
                    .catch(err => {})
                } else {
                    this.isLoading = false
                }
            })
            .catch(err => {})

            getWorkspaceOptions()
            .then(value => this.workspaces = value)
            .catch(err => {})

        },
        methods: {
            addToGeneratorConfig(key, value) {
                if (!(this.dataJob.generator in this.dataJob.config) || this.dataJob.config[this.dataJob.generator] == null) {
                    this.dataJob.config[this.dataJob.generator] = {}
                }
                let copyConfig = {...this.dataJob.config}
                copyConfig[this.dataJob.generator][key] = value
                this.$set(this.dataJob, 'config', copyConfig)
            },
            addToSinkConfig(key, value) {
                if (!(this.dataJob.sinks[0] in this.dataJob.config)) {
                    this.dataJob.config[this.dataJob.sinks[0]] = {}
                }
                let copyConfig = {...this.dataJob.config}
                copyConfig[this.dataJob.sinks[0]][key] = value
                this.$set(this.dataJob, 'config', copyConfig)

                //this.$set(this.dataJob.config[this.dataJob.sinks[0]], key, value)
            },
            escapeSpecialCharacters(value) {
                return value.replace('\n', '\\n')
            },
            getGeneratorOptions() {
                let options = []

                for (let generator of this.componentsData.generators) {
                    options.push({label: generator.name, value: generator.className, config: generator.configProperties})
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
            },
            getDatasets() {
                api.get(`/workspace/${this.workspace}/datasets`)
                .then(response => {
                    this.workspaceDatasets = response.data.payload
                })
                .catch(err => {})
            },
            getFile() {
                // window.open(`http://localhost:8080/datamaker/api/job-execution/${this.dataJob.externalId}/file`)
                api.get(`/job-execution/${this.dataJob.externalId}/file`,
                    {
                        responseType: 'blob',
                    }
                ).then((response) => {
                    const blob = new Blob([response.data], {type: response.data.type});
                    const url = window.URL.createObjectURL(blob);
                    const link = document.createElement('a');
                    link.href = url;
                    const contentDisposition = response.headers['content-disposition'];
                    let fileName = 'unknown';
                    if (contentDisposition) {
                        const fileNameMatch = contentDisposition.match(/filename="(.+)"/);
                        if (fileNameMatch.length === 2)
                            fileName = fileNameMatch[1];
                    }
                    link.setAttribute('download', fileName);
                    document.body.appendChild(link);
                    link.click();
                    link.remove();
                    window.URL.revokeObjectURL(url);
                });
            },
            getOutput() {
                api.get(`/job-execution/${this.dataJob.externalId}/output`)
                .then((response) =>
                    {
                        this.content = response.data
                        this.showContent = true
                    }
                )
            },
            getGlobalSinkOptions() {
                let options = []

                for (let sink of this.globalSinks) {
                    options.push({label: sink.name, value: sink.sinkClassName, config: sink.config})
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
                options.unshift({value: null, label: "Select one...", config: [], selected: "selected"})
                return options
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
                    options.push({id: sink.className, text: sink.name, label: sink.name, value: sink.className, config: sink.configProperties})
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
                options.unshift({id: null, text: "Select one...", value: null, label: "Select one...", config: [], disabled: "disabled", selected: "selected"})
                return options
            },
            selectAllDatasets(checked, input) {
                this.allSelected = !this.allSelected
                this.dataJob.datasets = []

                if (this.allSelected) {
                    for (let dataset in this.workspaceDatasets) {
                        this.dataJob.datasets.push(this.workspaceDatasets[dataset].externalId)
                    }
                }
            },
            updateCheckall(){
                this.allSelected = this.workspaceDatasets.length === this.dataJob.datasets.length
            },
            generatorSelected (item, index) {
                let configs = []
                let generator = this.getGeneratorOptions().find(element => element.value === item)

                if (!(generator.value in this.dataJob.config) || this.dataJob.config[generator.value] == null) {
                    this.dataJob.config[generator.value] = {}
                }
                if (generator.value !== this.dataJob.generator) {
                    delete this.dataJob.config[this.dataJob.generator]
                    //this.dataJob.config[generator.value] = {}
                }

                for (let input of generator.config) {
                    if (input.key in this.dataJob.config[generator.value]) {
                        input.value = this.dataJob.config[generator.value][input.key]
                    } else {
                        // TODO tostring could break arrays
                        // input.value = input.defaultValue.toString()
                      input.value = input.defaultValue === null ? "" : input.defaultValue
                        if (input.type === 'LIST') {
                            this.dataJob.config[generator.value][input.key] = []
                        }
                    }
                    configs.push(input)
                    //configs.push({value: input.defaultValue.toString(), key: input.key, label: input.description, placeholder: input.description})
                }
                this.dataJob.generator = generator.value
                this.generatorConfigs = configs
            },
            globalSinkSelect (item) {
                let configs = []
                let globalSink = this.getGlobalSinkOptions().find(element => element.value === item)
                if (!globalSink) {
                    return
                }

                let sink = this.getSinkOptions().find(element => element.value === item)

                if (!(sink.value in this.dataJob.config) || this.dataJob.config[sink.value] == null) {
                    this.dataJob.config[sink.value] = {}
                }
                if (sink.value !== this.dataJob.sinks[0]) {
                    delete this.dataJob.config[this.dataJob.sinks[0]]
                    //this.dataJob.config[generator.value] = {}
                }
                for (let input of sink.config) {
                    input.value = globalSink.config[input.key]
                    if (input.key in globalSink.config) {
                        this.dataJob.config[sink.value][input.key] = globalSink.config[input.key]
                        input.value = globalSink.config[input.key]
                    } else {
                        input.value = input.defaultValue === null ? "" : input.defaultValue.toString()
                    }
                    configs.push(input)
                }
                this.dataJob.sinks.shift()
                this.dataJob.sinks.push(sink.value)
                this.sinkConfigs = configs
            },
            sinkSelected (item) {
                let configs = []
                let sink = this.getSinkOptions().find(element => element.value === item)

                if (!(sink.value in this.dataJob.config) || this.dataJob.config[sink.value] == null) {
                    this.dataJob.config[sink.value] = {}
                }
                if (sink.value !== this.dataJob.sinks[0]) {
                    delete this.dataJob.config[this.dataJob.sinks[0]]
                    //this.dataJob.config[generator.value] = {}
                }
                for (let input of sink.config) {
                    if (input.key in this.dataJob.config[sink.value]) {
                        input.value = this.dataJob.config[sink.value][input.key]
                    } else {
                        input.value = input.defaultValue === null ? "" : input.defaultValue.toString()
                        if (input.type === 'LIST') {
                            this.dataJob.config[sink.value][input.key]  = []
                        }
                    }
                    configs.push(input)
                    //configs.push({value: input.defaultValue.toString(), key: input.key, label: input.description, placeholder: input.description})
                }
                // this.dataJob.sinks.shift()
                // this.dataJob.sinks.push(sink.value)
                this.sinkConfigs = configs
            },
            processTab(tab) {
                this.previousTab = tab
            },
            processPreviousTab() {
                this.currentTab = this.currentTab - 1
            },
            processNextTab() {
                if (this.validateTab(this.currentTab + 1)) {

                    processState(this, "beforeSubmit")
                    if (this.currentTab === 0) {
                      api.get(`/workspace/${this.dataJob.workspaceId}/datasets`)
                      .then(response => {
                        this.workspaceDatasets = response.data.payload
                      })
                      .catch(err => {})

                      if (!this.dataJob.externalId) {
                            api.post('/generate-data-job', this.dataJob)
                            .then(response => {
                                processState(this, "onSuccess")
                                this.currentTab = this.currentTab + 1
                                this.dataJob = response.data.payload
                            })
                            .catch(err => {
                                processState(this, "onError", err)
                            })
                        } else {
                            api.put(`/generate-data-job/${this.dataJob.externalId}`, this.dataJob)
                            .then(response => {
                                processState(this, "onSuccess")
                                this.currentTab = this.currentTab + 1
                            })
                            .catch(err => {
                                processState(this, "onError", err)
                            })
                        }
                    } else if (this.currentTab === 1) {
                        api.put(`/generate-data-job/${this.dataJob.externalId}/datasets`, this.dataJob.datasets)
                        .then(response => {
                            processState(this, "onSuccess")
                            this.currentTab = this.currentTab + 1
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        })
                    } else if (this.currentTab === 2) {
                        api.put(`/generate-data-job/${this.dataJob.externalId}/generator`, {
                            generatorClassName: this.dataJob.generator,
                            config: this.dataJob.config[this.dataJob.generator]
                        })
                        .then(response => {
                            processState(this, "onSuccess")
                            this.currentTab = this.currentTab + 1
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        })
                    } else if (this.currentTab === 3) {
                        api.put(`/generate-data-job/${this.dataJob.externalId}/sink`, {
                            sinkClassName: this.dataJob.sinks[0],
                            config: this.dataJob.config[this.dataJob.sinks[0]]
                        })
                        .then(response => {
                            processState(this, "onSuccess")
                            this.currentTab = this.currentTab + 1
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        })
                    }
                }
            },
            runJob() {
                api.post(`/job-execution/${this.dataJob.externalId}/schedule`)
                    .then(response => {
                        this.$eventHub.$emit('show-toast', "Job scheduled");
                    })
                    .catch(err => {})
            },
            safeId () {
                if (this.id || this.$attrs.id) {
                    return this.id || this.$attrs.id
                }
                return makeUid()
            },
            validateTab(tabIndex) {
                let form = this.$el.querySelector('#form-tab' + tabIndex)
                if (!form.checkValidity()) {
                    this['tab' + tabIndex + 'Errors']++
                    this['tab' + tabIndex + 'Valid'] = false
                    form.classList.add('was-validated')
                    return false
                }
                form.classList.add('was-validated')
                this['tab' + tabIndex +'Errors'] = 0
                this['tab' + tabIndex + 'Valid'] = true
                return true
            }
        },
        watch: {
            workspace: function() {
                this.getDatasets()
                api.get(`/sinks?workspaceId=${this.dataJob.workspaceId}`)
                    .then(response => {
                        this.globalSinks = response.data.payload
                    })
                    .catch(err => {})
            },
            scheduleType: function(newValue, oldValue) {
                if (newValue === 'Cron') {
                    this.dataJob.schedule = '0 * * * * *'
                } else if (newValue === 'Once') {
                    this.dataJob.schedule = 'once'
                } else {
                    this.dataJob.schedule = 'random ' + this.minDelay + ' ' + this.maxDelay
                }
            }
        }
    }
</script>

<style>
    input[type=range] {
        padding: 0 0;
    }
</style>