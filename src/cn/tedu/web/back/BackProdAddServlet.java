package cn.tedu.web.back;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadBase.FileSizeLimitExceededException;
import org.apache.commons.fileupload.FileUploadBase.SizeLimitExceededException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class BackProdAddServlet extends HttpServlet {

    public void doGet(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        doPost(request, response);
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored", "UnusedAssignment", "MismatchedQueryAndUpdateOfCollection"})
    public void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        //一、创建一个Map对象，保存form表单提交的数据
        Map<String, String> map = new HashMap<>();
        map.put("id", UUID.randomUUID().toString());
        //二、处理文件上传，并将form表单项中的内容保存到map中
        try {
            //1创建DiskFileItemFactory类对象，被设置参数
            //1.1创建对象
            DiskFileItemFactory factory = new DiskFileItemFactory();
            //1.2设置内存缓存区大小
            factory.setSizeThreshold(10 * 1024);//10KB
            //1.3设置临时文件的位置
            factory.setRepository(new File(getServletContext().getRealPath("/WEB-INF/tmp")));
            //2、创建ServletFileUpload对象,并设置它的参数以及处理request数据
            //2.1创建ServletFileUpload类的对象
            ServletFileUpload fileUpload = new ServletFileUpload(factory);
            //2.2、设置单个文件最大值
            fileUpload.setFileSizeMax(1024 * 1024);
            //2.2、设置form上传文件总的最大值
            fileUpload.setSizeMax(10 * 1024 * 1024);
            //2.3、处理文件名乱码的问题
            fileUpload.setHeaderEncoding("UTF-8");
            //2.4、文件上传监听
			/*fileUpload.setProgressListener(new ProgressListener(){
				public void update(long pBytesRead, long pContentLength, int pItems) {
				}});*/
            //2.5、处理request对象
            List list = fileUpload.parseRequest(request);
            //3、遍历处理list
            for (Object obj : list) {
                FileItem item = (FileItem) obj;
                if (item.isFormField()) {//普通表单项
                    map.put(item.getFieldName(), item.getString("UTF-8"));
                } else {//文件上传表单项
                    StringBuilder path = new StringBuilder("/WEB-INF/upload");
                    //处理文件名称   c:/jinxf/abc.jpg
                    String filename = item.getName();
                    //处理部分浏览器的bug问题(文件名中带有本地路径的问题)
                    if (filename.contains("\\")) {
                        filename = filename.substring(filename.lastIndexOf("\\") + 1);
                    }
                    //处理文件重名的问题
                    filename = UUID.randomUUID().toString() + "_" + filename;
                    //保存目录的问题
                    String hash = Integer.toHexString(filename.hashCode());
                    char[] chs = hash.toCharArray();
                    for (char ch : chs) {
                        path.append("/").append(ch);
                    }// /WEB-INF/upload/a/b/f/...
                    new File(getServletContext().getRealPath(path.toString())).mkdirs();
                    //IO操作
                    InputStream in = item.getInputStream();
                    OutputStream out = new FileOutputStream(
                            getServletContext().getRealPath(path + "/" + filename));
                    byte[] bts = new byte[1024];
                    int len = -1;
                    while ((len = in.read(bts)) != -1) {
                        out.write(bts, 0, len);
                    }
                    //关闭流
                    out.close();
                    in.close();
                    //删除临时文件
                    item.delete();
                    //map.put("imgurl", path+"/"+filename);
                    map.put(item.getFieldName(), path + "/" + filename);
                }
            }
            //三、添加商品信息到数据库的表products
            //四、提示并跳转
            response.getWriter().write("添加成功，3秒后自动跳转，如果未自动跳转，" +
                    "请<a href='" + request.getContextPath() + "/servlet/BackProdListServlet'>点击此处</a>");
            response.setHeader("Refresh", "3;url=" + request.getContextPath() + "/servlet/BackProdListServlet");
        } catch (FileSizeLimitExceededException e) {
            e.printStackTrace();
            //四、提示并跳转
            response.getWriter().write("单个上传文件大小超限！商品图片不能大于1MB");
        } catch (SizeLimitExceededException e) {
            e.printStackTrace();
            //四、提示并跳转
            response.getWriter().write("上传文件大小超限！商品图片不能大于10MB");
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("###");
        }
    }

}
