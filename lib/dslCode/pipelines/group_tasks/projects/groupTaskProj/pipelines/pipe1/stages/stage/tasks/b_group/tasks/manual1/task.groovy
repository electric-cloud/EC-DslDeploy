
task 'manual1', {
  notificationEnabled = '1'
  notificationTemplate = 'ec_default_pipeline_manual_task_notification_template'
  taskType = 'MANUAL'
  approver = [
    'admin',
  ]
}
