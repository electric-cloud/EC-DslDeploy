
task 'task2', {
  gateType = 'PRE'
  notificationEnabled = '1'
  notificationTemplate = 'ec_default_gate_task_notification_template'
  taskType = 'APPROVAL'
  projectName = 'Test2'
  subproject = 'Test2'
  approver = [
    'admin',
  ]
}
