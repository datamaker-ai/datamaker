<template>
    <CRow>
        <CCol col="12" lg="6">
            <CCard>
                <CCardHeader>
                  <span v-html="$t('users.edit_group_header')"></span>
                </CCardHeader>
                <CCardBody>
                    <CAlert :show="displayError" color="danger">
                      <div>{{ $t('common.error_message_template', { msg: errorMessage}) }}</div>
                      <div v-if="errorDetails">{{ $t('common.error_details_template', { msg: errorDetails }) }}</div>
                    </CAlert>
                    <CForm id="user">
                        <CInput
                                :description="$t('users.group_name_description')"
                                :label="$t('users.group_name_label')"
                                horizontal
                                :placeholder="$t('users.group_name_placeholder')"
                                v-model="group.name"
                                required
                        />
                        <CInput
                                :description="$t('users.group_description_description')"
                                :label="$t('users.group_description_label')"
                                horizontal
                                :placeholder="$t('users.group_description_placeholder')"
                                v-model="group.description"
                                required
                        />
                    </CForm>
                </CCardBody>
                <CCardFooter>
                    <CButton type="submit" size="sm" color="primary" @click="submitForm" :disabled="disabledButtons" class="mr-1">
                        <CIcon name="cil-check-circle"/>
                      {{ $t('common.submit_button') }}
                    </CButton>
                    <CButton type="reset" size="sm" color="danger" @click="resetForm" :disabled="disabledButtons">
                        <CIcon name="cil-ban"/>
                      {{ $t('common.reset_button') }}
                    </CButton>
                </CCardFooter>
            </CCard>
        </CCol>
    </CRow>
</template>

<script>
    import {api, processState} from "../../constants/api";
    import {Translation} from '../../plugins/Translation'

    export default {
        name: "EditGroup",
        data: () => {
            return {
                group: {
                    name: '',
                    description: ''
                },
                errorMessage: '',
                errorDetails: '',
                disabledButtons: false,
                displayError: false
            }
        },
        created() {
            if (this.$route.params.id) {
                api.get(`user/group/${this.$route.params.id}`)
                    .then(response => {
                        this.group = response.data.payload
                    })
                    .catch(err => {
                    })
            }
        },
        methods: {
            resetForm: function (event) {
                let form = this.$el.querySelector('form')
                form.reset()
            },
            submitForm: function (event) {
                processState(this, "beforeSubmit")

                let form = this.$el.querySelector('form')
                if (!form.checkValidity()) {
                    form.classList.add('was-validated')
                    this.disabledButtons = false
                    return false
                }

                if (this.$route.params.id) {
                    api.put('user/group', this.group)
                        .then(response => {
                            this.$router.push('/users/groups')
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        });
                } else {
                    api.post('user/group', this.group)
                        .then(response => {
                            this.$router.push('/users/groups')
                        })
                        .catch(err => {
                            processState(this, "onError", err)
                        });
                }
            }
        }
    }
</script>

<style scoped>

</style>