
task 'postTask', {
  notificationEnabled = '1'
  notificationTemplate = 'ec_default_gate_task_notification_template'
  taskType = 'APPROVAL'
  approver = [
    'testUser',
  ]
}
