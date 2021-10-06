var authSvcUrl;
var prodtuctSvcUrl;
var cartSvcUrl;

if (process.env.REACT_APP_GATEWAY_SVC_ORIGIN == null) {
	authSvcUrl = process.env.REACT_APP_AUTH_SVC_ORIGIN + "/auth/";
	prodtuctSvcUrl = process.env.REACT_APP_PRODUCT_SVC_ORIGIN + "/products/";
	cartSvcUrl = process.env.REACT_APP_CART_SVC_ORIGIN + "/carts/";
} else {
	authSvcUrl = process.env.REACT_APP_GATEWAY_SVC_ORIGIN + "/auth/";
	prodtuctSvcUrl = process.env.REACT_APP_GATEWAY_SVC_ORIGIN + "/products/";
	cartSvcUrl = process.env.REACT_APP_GATEWAY_SVC_ORIGIN + "/carts/";	
}

export const AUTH_SVC_URL = authSvcUrl;
export const PRODUCT_SVC_URL = prodtuctSvcUrl;
export const CART_SVC_URL = cartSvcUrl;

export const PRODUCT_IMAGE_BASE_URL = process.env.REACT_APP_IMAGES_URL;
