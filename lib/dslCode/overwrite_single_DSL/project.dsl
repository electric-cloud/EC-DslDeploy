project 'BEE-19095', {
    tracked = '1'

    pipeline 'test', {

        formalParameter 'ec_stagesToRun', {
            expansionDeferred = '1'
        }

        stage 'Stage 1_changed', {
            colorCode = '#289ce1'
            pipelineName = 'test'

            gate 'PRE'

            gate 'POST'
        }
    }
}