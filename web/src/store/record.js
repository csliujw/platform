export default {
    state: {
        is_record: false,
        asteps: "",
        bsteps: "",
        record_loser: ""
    },
    getters: {},
    mutations: {
        updateIsRecord(state, is_record) {
            state.is_record = is_record;
        },
        updateSteps(state, data) {
            state.asteps = data.asteps;
            state.bsteps = data.bsteps;
        },
        updateRecordLoser(state, data) {
            state.record_loser = data.record_loser;
        }
    },
    actions: {},
    modules: {}
}