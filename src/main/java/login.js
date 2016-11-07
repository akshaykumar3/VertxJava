// Below function Executes on click of login button.
//$('#loginButton').on('click',


	function validate(){
	 var self = this;
	 var data = {"emailId":"akshay0007k@gmail.com","password":"1234"};
        self.username = document.getElementById("username").value;
        self.password = document.getElementById("password").value;
   $.ajax({
   type:'POST',
   url :"http://192.168.0.161:8080/merchantLogin",

   contentType: "application/json",

                dataType: 'json',
                data: data,


   success: function(response) {
        console.log('success',response);

   },
   error:function(exception){alert('Exeption:'+exception);}
});
 //e.preventDefault();
}
//);