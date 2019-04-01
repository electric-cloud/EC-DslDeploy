/*
def hostType="CONCURRENT"
if (System.getenv("HOSTTYPE")) {
  hostType=System.getenv("HOSTTYPE")
}
*/
resource 'res457',
  hostName: 'doesnotexist'
//  hostType: "$hostType"
