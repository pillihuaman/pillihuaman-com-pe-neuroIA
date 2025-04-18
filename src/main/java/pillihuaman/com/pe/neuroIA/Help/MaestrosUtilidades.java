package pillihuaman.com.pe.neuroIA.Help;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.*;

public class MaestrosUtilidades {
	
	private static final Logger logger = LoggerFactory.getLogger(MaestrosUtilidades.class);
	private static ObjectMapper mapper = new ObjectMapper();
	public static final Locale LOCALE_PE = new Locale("es", "PE");
/*
	// Utilidades de Json
	public static String getJson(Object object) {
		String result = "";
		try {
			mapper.setSerializationInclusion(Include.NON_NULL);
			result = mapper.writeValueAsString(object);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public static <T> List<T> jsonToList(String jsonString, Class<?> clazz) {
		List<T> result = null;
		try {
			mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
			JavaType type = mapper.getTypeFactory().constructCollectionType(List.class, clazz);
			result = mapper.readValue(jsonString, type);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	public static String getJsonWithNull(Object object) {
		String result = "";
		try {
			mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
			result = mapper.writeValueAsString(object);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	@SuppressWarnings("unchecked")
	public static <T> T jsonToObject(String jsonString, Class<?> clazz) {
		T result = null;
		try {
			mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
			result = (T) mapper.readValue(jsonString, clazz);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	// PARSEADORES
	public static Long toLong(Object objeto) {
		if (isEmpty(objeto)) {
			return null;
		}
		try {
			return Long.parseLong(objeto.toString());
		} catch (NumberFormatException e) {
			logger.error("Ocurri\u00f3 un error en Utilidades.toLong", e);
			return null;
		}
	}

	public static Short toShort(Object objeto) {
		if (isEmpty(objeto)) {
			return null;
		}
		try {
			return Short.parseShort(objeto.toString());
		} catch (NumberFormatException e) {
			logger.error("Ocurri\u00f3 un error en Utilidades.toShort", e);
			return null;
		}
	}

	public static Boolean toBoolean(Object objeto) {
		if (isEmpty(objeto)) {
			return null;
		}
		try {
			return Boolean.parseBoolean(objeto.toString());
		} catch (Exception e) {
			logger.error("Ocurri\u00f3 un error en Utilidades.toBoolean", e);
			return null;
		}
	}

	public static Integer toInteger(Object objeto) {
		if (isEmpty(objeto)) {
			return null;
		}
		try {
			return Integer.parseInt(objeto.toString());
		} catch (Exception e) {
			logger.error("Ocurrio un error en Utilidades.toInteger", e);
			return null;
		}
	}

	public static Double toDouble(Object objeto) {
		if (isEmpty(objeto)) {
			return null;
		}
		try {
			return Double.parseDouble(objeto.toString());
		} catch (NumberFormatException e) {
			logger.error("Ocurri\u00f3 un error en Utilidades.toDouble", e);
			return null;
		}
	}

	public static Float toFloat(Object objeto) {
		if (isEmpty(objeto)) {
			return null;
		}
		try {
			return Float.parseFloat(objeto.toString());
		} catch (NumberFormatException e) {
			logger.error("Ocurri\u00f3 un error en Utilidades.toFloat", e);
			return null;
		}
	}

	public static String toStr(Object cadena) {
		return isEmpty(cadena) ? null : toBlank(cadena.toString());
	}

	public static String toBlank(Object object) {
		return isEmpty(object) ? "" : object.toString();
	}

	public static String toDefault(Object cadena, String strDefault) {
		return isEmpty(cadena) ? strDefault : cadena.toString().trim();
	}
	// PARSEADORES

	// Validar Vacio
	public static boolean isEmpty(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof String) {
			return object.toString().trim().length() == 0;
		}
		if (object instanceof StringBuilder) {
			return object.toString().trim().length() == 0;
		}
		if (object instanceof List<?> || object instanceof ArrayList<?>) {
			return ((List<?>) object).isEmpty();
		}
		if (object instanceof Map<?, ?> || object instanceof HashMap<?, ?>) {
			return ((Map<?, ?>) object).isEmpty();
		}
		return false;
	}

	// Validar Equivalencias
	public static boolean equiv(Object object1, Object object2) {
		if (object1 == null || object2 == null) {
			return false;
		}
		if (isEmpty(object1) && !isEmpty(object2) || !isEmpty(object1) && isEmpty(object2)) {
			return false;
		}
		if (isEmpty(object1) && isEmpty(object2) || object1 == object2) {
			return true;
		}

		if (object1 instanceof String && object2 instanceof String) {
			return toBlank(object1.toString()).equals(toBlank(object2.toString()));
		}
		return object1.equals(object2);
	}

	// Manejo String
	public static String tildesEnFormatoHtml(String str) {
		String strFormat = str;
		strFormat = strFormat.replace("\u00e1", "&aacute;");
		strFormat = strFormat.replace("\u00e9", "&eacute;");
		strFormat = strFormat.replace("\u00ed", "&iacute;");
		strFormat = strFormat.replace("\u00f3", "&oacute;");
		strFormat = strFormat.replace("\u00fa", "&uacute;");
		strFormat = strFormat.replace("\u00c1", "&Aacute;");
		strFormat = strFormat.replace("\u00c9", "&Eacute;");
		strFormat = strFormat.replace("\u00cd", "&Iacute;");
		strFormat = strFormat.replace("\u00d3", "&Oacute;");
		strFormat = strFormat.replace("\u00da", "&Uacute;");
		strFormat = strFormat.replace("\u00f1", "&ntilde;");
		strFormat = strFormat.replace("\u00d1", "&Ntilde;");
		strFormat = strFormat.replace("\u00B0", "&deg;");
		return strFormat;
	}

	public static String capitalize(String text) {
		String capitalized = Arrays.asList(text.split(" ")).stream()
				.map(x -> x.length() > 2 ? (x.substring(0, 1).toUpperCase() + x.substring(1).toLowerCase()) : x)
				.reduce((x, y) -> x + " " + y).orElse("");
		return capitalized;
	}
	// Manejo String

	// INI FECHAS
	public static String dateToString(Date fechaDate, String formatString) {
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		try {
			return format.format(fechaDate);
		} catch (Exception e) {
			logger.error("Error MaestrosUtilidades - dateToString", e.getMessage(),e);
			return null;
		}
	}

	public static Date stringToDate(String fechaString, String formatString) {
		SimpleDateFormat format = new SimpleDateFormat(formatString);
		try {
			return format.parse(fechaString);
		} catch (Exception e) {
			logger.error("Fin MaestrosUtilidades - stringToDate [Exception]:" + e.getMessage(), e);
			return null;
		}
	}

/*	public static String dateToStringDDMMYYYY(Date fechaDate) {
		return dateToString(fechaDate, MaestrosConstantes.FORMAT_FECHA_DDMMYYYY);
	}

	public static String dateToStringYYYYMMDD(Date fechaDate) {
		return dateToString(fechaDate, MaestrosConstantes.FORMAT_FECHA_YYYYMMDD);
	}

	public static String dateToStringHHMM(Date fechaDate) {
		return dateToString(fechaDate, MaestrosConstantes.FORMAT_HORA_HHMM);
	}
	
	public static String dateToStringHHMMSS(Date fechaDate) {
		return dateToString(fechaDate, MaestrosConstantes.FORMAT_HORA_HHMMSS);
	}

	public static Date stringToDateDDMMYYYY(String fechaString) {
		return stringToDate(fechaString, MaestrosConstantes.FORMAT_FECHA_DDMMYYYY);
	}

	public static Date stringToDateYYYYMMDD(String fechaString) {
		return stringToDate(fechaString, MaestrosConstantes.FORMAT_FECHA_YYYYMMDD);
	}

	public static Date truncateDateDDMMYYYY(Date fechaDate) {
		return stringToDateDDMMYYYY(dateToStringDDMMYYYY(fechaDate));
	}*/

	public static Date fechaActual() {
		return new Date();
	}

	public static Date fechaInicialDia(Date fecha) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.set(Calendar.HOUR, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MILLISECOND, 0);
		return calendar.getTime();
	}

	public static Date fechaFinalDia(Date fecha) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.set(Calendar.HOUR, 23);
		calendar.set(Calendar.MINUTE, 59);
		calendar.set(Calendar.SECOND, 59);
		calendar.set(Calendar.MILLISECOND, 99);
		return calendar.getTime();
	}

	public static Date addDay(Date fecha, int dias) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.DAY_OF_MONTH, dias);
		return calendar.getTime();
	}

	public static Date quitarDias(Date fecha, int dias) {

		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.DAY_OF_YEAR, -dias);
		return calendar.getTime();

	}

	public static Date addMonth(Date fecha, int meses) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.MONTH, meses);
		return calendar.getTime();
	}

	public static Date addYear(Date fecha, int anios) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(fecha);
		calendar.add(Calendar.YEAR, anios);
		return calendar.getTime();
	}

	public static Date obtenerPrimerDiaMes(Date fechaEnviada) {
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaEnviada);
		int anio = fecha.get(Calendar.YEAR);
		int mes = fecha.get(Calendar.MONTH) + 1;
		return numeroYYYYMMDDToDate(anio, mes, 1);
	}

	public static Date obtenerUltimoDiaMes(Date fechaEnviada) {
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaEnviada);
		int anio = fecha.get(Calendar.YEAR);
		int mes = fecha.get(Calendar.MONTH) + 1;
		Calendar calendario = Calendar.getInstance();
		calendario.set(anio, mes - 1, 1);
		int ultimoDia = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
		return numeroYYYYMMDDToDate(anio, mes, ultimoDia);
	}

	public static Date obtenerPrimerDiaAnio(Date fechaEnviada) {
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaEnviada);
		int anio = fecha.get(Calendar.YEAR);
		return numeroYYYYMMDDToDate(anio, 1, 1);
	}

	public static Date obtenerUltimoDiaAnio(Date fechaEnviada) {
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(fechaEnviada);
		int anio = fecha.get(Calendar.YEAR);
		Calendar calendario = Calendar.getInstance();
		calendario.set(anio, 12, 1);
		int ultimoDia = calendario.getActualMaximum(Calendar.DAY_OF_MONTH);
		return numeroYYYYMMDDToDate(anio, 12, ultimoDia);
	}

	public static Date obtenerPrimerDiaMesDePeriodo(String periodo) {
		// Periodo esta en el formato anio y mes (202001)
		Date resultado = null;
		if (periodo != null) {
			Integer anio = Integer.parseInt(periodo.substring(0, 4));
			Integer mes = Integer.parseInt(periodo.substring(4, 6));
			Date fechaEnviar = numeroYYYYMMDDToDate(anio, mes, 1);
			resultado = obtenerPrimerDiaMes(fechaEnviar);
		}
		return resultado;
	}

	public static Date obtenerUltimoDiaMesDePeriodo(String periodo) {
		// Periodo esta en el formato anio y mes (202001)
		Date resultado = null;
		if (periodo != null) {
			Integer anio = Integer.parseInt(periodo.substring(0, 4));
			Integer mes = Integer.parseInt(periodo.substring(4, 6));
			Date fechaEnviar = numeroYYYYMMDDToDate(anio, mes, 1);
			resultado = obtenerUltimoDiaMes(fechaEnviar);
		}
		return resultado;
	}

	public static Date obtenerPrimerDiaAnioDePeriodo(String periodo) {
		// Periodo esta en el formato anio y mes (202001)
		Date resultado = null;
		if (periodo != null) {
			Integer anio = Integer.parseInt(periodo.substring(0, 4));
			Integer mes = Integer.parseInt(periodo.substring(4, 6));
			Date fechaEnviar = numeroYYYYMMDDToDate(anio, mes, 1);
			resultado = obtenerPrimerDiaAnio(fechaEnviar);
		}
		return resultado;
	}

	public static Date obtenerUltimoDiaAnioDePeriodo(String periodo) {
		// Periodo esta en el formato anio y mes (202001)
		Date resultado = null;
		if (periodo != null) {
			Integer anio = Integer.parseInt(periodo.substring(0, 4));
			Integer mes = Integer.parseInt(periodo.substring(4, 6));
			Date fechaEnviar = numeroYYYYMMDDToDate(anio, mes, 1);
			resultado = obtenerUltimoDiaAnio(fechaEnviar);
		}
		return resultado;
	}

	public static Date numeroYYYYMMDDToDate(int anio, int mes, int dia) {
		Calendar calendar = Calendar.getInstance();
		calendar.set(anio, mes - 1, dia);
		return calendar.getTime();
	}

	public static String obtenerAnioMesAAAAMM(Date date) {
		Calendar fecha = Calendar.getInstance();
		fecha.setTime(date);
		String anio = String.valueOf(fecha.get(Calendar.YEAR));
		Integer mesTmp= fecha.get(Calendar.MONTH) + 1;
		String mes = (new DecimalFormat("00")).format(mesTmp);
		return anio.concat(mes);
	}
/*
	public static Date obtenerFechaVacia() {
		String fechaVacia = MaestrosConstantes.FECHA_POR_DEFECTO;
		return stringToDateDDMMYYYY(fechaVacia);
	}
	
	public static LocalDate obtenerLocalDateVacia() {
		return LocalDate.parse(
				MaestrosConstantes.FECHA_HORA_POR_DEFECTO,
				DateTimeFormatter.ofPattern(MaestrosConstantes.FORMAT_FECHAHORA_DDMMYYYYHHMMSS));
	}
	
	public static LocalDateTime obtenerLocalDateTimeVacia() {
		return LocalDateTime.parse(
				MaestrosConstantes.FECHA_HORA_POR_DEFECTO,
				DateTimeFormatter.ofPattern(MaestrosConstantes.FORMAT_FECHAHORA_DDMMYYYYHHMMSS));
	}*/
	
	public static LocalDate dateToLocalDate(Date date) {
		if ( date == null ) {
			return null;
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
	}
	
	public static Date localDateToDate(LocalDate localDate) {
		if ( localDate == null ) {
			return null;
		}
		return Date.from(localDate.atStartOfDay().atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static LocalDateTime dateToLocalDateTime(Date date) {
		if ( date == null ) {
			return null;
		}
		return date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
	}
	
	public static Date localDateTimeToDate(LocalDateTime localDateTime) {
		if ( localDateTime == null ) {
			return null;
		}
		return Date.from(localDateTime.atZone(ZoneId.systemDefault()).toInstant());
	}
	
	public static LocalDate getPrimerDiaDelMesLocalDate() {
		return getPrimerDiaDelMesLocalDate(LocalDate.now());
	}
	
	public static LocalDate getUltimoDiaDelMesLocalDate() {
		return getPrimerDiaDelMesLocalDate(LocalDate.now());
	}
	
	public static LocalDate getPrimerDiaDelMesLocalDate(YearMonth periodo) {
		return YearMonth.from(periodo).atDay(1);
	}
	
	public static LocalDate getUltimoDiaDelMesLocalDate(YearMonth periodo) {
		return YearMonth.from(periodo).atEndOfMonth();
	}
	
	public static LocalDate getPrimerDiaDelMesLocalDate(LocalDate fechaReferencia) {
		return getPrimerDiaDelMesLocalDate(YearMonth.from(fechaReferencia));
	}
	
	public static LocalDate getUltimoDiaDelMesLocalDate(LocalDate fechaReferencia) {
		return getUltimoDiaDelMesLocalDate(YearMonth.from(fechaReferencia));
	}
	
	/*public static YearMonth stringToYearMonth(String periodo) {
		return YearMonth.parse(periodo, 
				DateTimeFormatter.ofPattern(MaestrosConstantes.FORMAT_FECHA_YYYYMM));
	}
	
	public static String yearMonthToString(YearMonth periodo) {
		return periodo.format(DateTimeFormatter.ofPattern(MaestrosConstantes.FORMAT_FECHA_YYYYMM));
	}
	*/
	// FIN FECHAS

	// Validacion modulo RUC
	public static boolean validarModuloRUC(final String numRuc) {
		Integer wresto = 0;
		if (numRuc.length() < 11) {
			return false;
		}
		String cadena = numRuc.substring(0, 10).trim();
		if (cadena.trim().length() == 10) {
			wresto = (2 * Integer.parseInt(cadena.substring(9, 10))) + (3 * Integer.parseInt(cadena.substring(8, 9)))
					+ (4 * Integer.parseInt(cadena.substring(7, 8))) + (5 * Integer.parseInt(cadena.substring(6, 7)))
					+ (6 * Integer.parseInt(cadena.substring(5, 6))) + (7 * Integer.parseInt(cadena.substring(4, 5)))
					+ (2 * Integer.parseInt(cadena.substring(3, 4))) + (3 * Integer.parseInt(cadena.substring(2, 3)))
					+ (4 * Integer.parseInt(cadena.substring(1, 2))) + (5 * Integer.parseInt(cadena.substring(0, 1)));
			wresto = wresto % 11;
			wresto = 11 - wresto;
			if (wresto >= 10)
				wresto = wresto - 10;
		}
		return (wresto.toString().equals(String.valueOf(numRuc.charAt(10))));
	}


	
	public static StringBuilder iniciarMensajeCorreo() {
		StringBuilder mensaje = new StringBuilder();
		mensaje.append("<html lang='es'>");
		mensaje.append("<head>");
		mensaje.append("<meta charset='utf-8'>");
		mensaje.append("<meta http-equiv='X-UA-Compatible' content='IE=edge'>");
		mensaje.append("<meta http-equiv='X-UA-Compatible' content='IE=8'>");
		mensaje.append("</head>");
		mensaje.append("<body>");

		return mensaje;
	}

	public static StringBuilder cerrarMensajeCorreo(StringBuilder mensaje) {
		mensaje.append("</body>");
		mensaje.append("</html>");
		return mensaje;
	}

	public static boolean fechaIgual(Date fini, Date ffin) {
		boolean result = false;
		if (fini.compareTo(ffin) == 0)
			result = true;
		return result;
	}

	public static boolean fechaMenor(Date fini, Date ffin) {
		boolean result = false;
		if (fini.compareTo(ffin) < 0)
			result = true;
		return result;
	}

	public static boolean fechaMayor(Date fini, Date ffin) {
		boolean result = false;
		if (fini.compareTo(ffin) > 0)
			result = true;
		return result;
	}

	public static String localServerIP() {
		String ipRemote = "";
		try {
			InetAddress address = InetAddress.getLocalHost();
			ipRemote = address.getHostAddress();
		} catch (UnknownHostException e) {
			logger.error(e.toString(), e);
		}
		return ipRemote;
	}



	public static String obtenerNombreMes(int numberOfMonth) {
		String nameOfMonth = "";
		switch (numberOfMonth) {
		case 1:
			nameOfMonth = "Enero";
			break;
		case 2:
			nameOfMonth = "Febrero";
			break;
		case 3:
			nameOfMonth = "Marzo";
			break;
		case 4:
			nameOfMonth = "Abril";
			break;
		case 5:
			nameOfMonth = "Mayo";
			break;
		case 6:
			nameOfMonth = "Junio";
			break;
		case 7:
			nameOfMonth = "Julio";
			break;
		case 8:
			nameOfMonth = "Agosto";
			break;
		case 9:
			nameOfMonth = "Setiembre";
			break;
		case 10:
			nameOfMonth = "Octubre";
			break;
		case 11:
			nameOfMonth = "Noviembre";
			break;
		case 12:
			nameOfMonth = "Diciembre";
			break;
		}
		return nameOfMonth;
	}

	public static String decodeBase64(String encodeBase64) {
		if (logger.isDebugEnabled())
			logger.debug("Inicio MaestrosUtilidades - decodeBase64");
		try {
			return new String(Base64.getDecoder().decode(encodeBase64.trim()));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	public static String encodeBase64(String decodeBase64) {
		if (logger.isDebugEnabled())
			logger.debug("Inicio MaestrosUtilidades - encodeBase64");
		try {
			return Base64.getEncoder().encodeToString(decodeBase64.getBytes());
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}

	/*public static byte[] obtenerBytesFile(String filePath) {

		FileInputStream fileInputStream = null;
		byte[] bytesArray = null;

		try {
			File file = new File(filePath);
			fileInputStream = new FileInputStream(file);
			bytesArray = IOUtils.toByteArray(fileInputStream);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (fileInputStream != null) {
				try {
					fileInputStream.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return bytesArray;
	}
*/
	public static String obtenerSiguientePeriodo(String periodo) {

		if (logger.isDebugEnabled())
			logger.debug("Inicio MaestrosUtilidades - obtenerSiguientePeriodo ");

		String periodoSiguiente;
		String periodoInicio = "12";

		String anioPeriodoInicio = periodo.substring(0, 4);
		String mesPeriodoInicio = periodo.substring(4);

		Integer anioEntero = Integer.parseInt(anioPeriodoInicio);
		Integer mesEntero = Integer.parseInt(mesPeriodoInicio);

		if (mesPeriodoInicio.equals(periodoInicio)) {
			anioEntero = anioEntero + 1;
			anioPeriodoInicio = Integer.toString(anioEntero);
			periodoSiguiente = anioPeriodoInicio + "01";

		} else {
			mesEntero = mesEntero + 1;
			mesPeriodoInicio = "00".substring(Integer.toString(mesEntero).length()) + Integer.toString(mesEntero);
			periodoSiguiente = anioPeriodoInicio + mesPeriodoInicio;
		}

		return periodoSiguiente;
	}

	/*public static File descomprimirArchivoZip(File archivo) {

		File archivoNuevo = null;
		byte[] buffer = new byte[1024];
		ZipInputStream zis = null;
		FileOutputStream fos = null;
		FileInputStream inputStream = null;
		try {
			inputStream = new FileInputStream(archivo);
			zis = new ZipInputStream(inputStream);
			ZipEntry ze = zis.getNextEntry();
			String nombreArchivo = ze.getName();
			archivoNuevo = new File(MaestrosConstantes.URL_TEMP + File.separator + nombreArchivo);
			fos = new FileOutputStream(archivoNuevo);

			int len;
			while ((len = zis.read(buffer)) > 0) {
				fos.write(buffer, 0, len);
			}

		} catch (IOException ex) {
			logger.error("Error MaestrosUtilidades - descomprimirArchivoZip [Exception]:" + ex.getMessage(), ex);
		} finally {
			cerrarStream(fos);
			cerrarStream(zis);
			cerrarStream(inputStream);
		}

		return archivoNuevo;
	}

	public static <E> E obtenerPrimerElemento(List<E> lista) {
		if (lista == null || lista.isEmpty()) {
			return null;
		}
		return lista.get(0);
	}
	
	public static Calendar dateToCalendar(Date date) {
		Calendar calendar = Calendar.getInstance();
		calendar.setTime(date);
		return calendar;
	}
	
	public static int calcularEdad(Calendar fechaNac) {	
        Calendar fechaActual = Calendar.getInstance(); 
        int years = fechaActual.get(Calendar.YEAR) - fechaNac.get(Calendar.YEAR);
        int months = fechaActual.get(Calendar.MONTH) - fechaNac.get(Calendar.MONTH);
        int days = fechaActual.get(Calendar.DAY_OF_MONTH) - fechaNac.get(Calendar.DAY_OF_MONTH); 
        if(months < 0 || (months==0 && days < 0))
        	years--;        
        return years;
    }
	
	public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
	    Set<Object> seen = ConcurrentHashMap.newKeySet();
	    return t -> seen.add(keyExtractor.apply(t));
	}

	public static void cerrarStream(Closeable closable) {
		if (null != closable) {
			try {
				closable.close();
			} catch (IOException e) {
				logger.error("Ocurrio un error.cerrarStream: ", e);
			}
		}
	}
	public static void cerrarStream(AutoCloseable closable) {
		if (null != closable) {
			try {
				closable.close();
			} catch (Exception e) {
				logger.error("Ocurrio un error.cerrarStream: ", e);
			}
		}
	}*/
	public static boolean isEmpty(Object object) {
		if (object == null) {
			return true;
		}
		if (object instanceof String) {
			return object.toString().trim().length() == 0;
		}
		if (object instanceof StringBuilder) {
			return object.toString().trim().length() == 0;
		}
		if (object instanceof List<?> || object instanceof ArrayList<?>) {
			return ((List<?>) object).isEmpty();
		}
		if (object instanceof Map<?, ?> || object instanceof HashMap<?, ?>) {
			return ((Map<?, ?>) object).isEmpty();
		}
		return false;
	}
}
