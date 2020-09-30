const bdshop = {
        formatPrice(val){
            const last = val.lastIndexOf(".");
            let pe = "";
            if(val < 0){
                pe = val + "00";
            }else if(last === pe.length - 2){
                pe = val.replace("\.","") + "0";
            }else{
                pe = val.replace("\.","");
            }
            return parseInt(pe);
        },
}