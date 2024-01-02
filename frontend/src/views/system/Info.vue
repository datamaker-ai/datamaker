<template>
  <div>
    <CRow>
      <CCol md="6">
        <CCard>
          <CCardHeader>
            <CIcon name="cil-justify-center"/>
            <strong>{{ $t('system.system_info_header') }}</strong>
          </CCardHeader>
          <CCardBody>
            <CListGroup>
              <CListGroupItem>{{ $t('system.build_number', { number: info.build.build.number }) }}</CListGroupItem>
              <CListGroupItem>{{ $t('system.build_time', { time: info.build.time }) }}</CListGroupItem>
              <CListGroupItem>{{ $t('system.java', { input: info.build.java.target }) }}</CListGroupItem>
              <CListGroupItem>{{ $t('system.profile', { input: info.build.profile }) }}</CListGroupItem>
              <CListGroupItem>{{ $t('system.version', { version: info.build.version }) }}</CListGroupItem>
            </CListGroup>
          </CCardBody>
        </CCard>
      </CCol>
    </CRow>
  </div>
</template>

<script>
    import * as api from "../../constants/api";

    export default {
      name: "Info",
      data() {
        return {
          info: { build: {version: '1.0', time: new Date(), build: {number: 1}}}
        }
      },
      mounted() {
        api.metrics("/info")
        .then(response => {
          this.info = response.data
        })
        .catch(err => {})
      },
    }
</script>

<style scoped>

</style>