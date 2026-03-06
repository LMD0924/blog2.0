package com.example.backend.controller;

import com.example.backend.DTO.ArticleDTO;
import com.example.backend.entity.Article;
import com.example.backend.entity.ArticleInfo;
import com.example.backend.entity.RestBean;
import com.example.backend.entity.UserAndArticle;
import com.example.backend.service.ArticleService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.List;

@RestController
@RequestMapping("api/article")
public class ArticleController {
    @Resource
    ArticleService articleService;
    @PostMapping("addArticle")
    public RestBean<String> addArticle(@RequestBody ArticleDTO articledto){
        int status=articleService.InsertArticle(articledto.getArticle(),articledto.getArticleInfo());
        if(status==1) return RestBean.success("成功发布文章");
        else return RestBean.failure(503,"发布文章失败");
    }
    @GetMapping("getAllArticle")
    public RestBean<List<UserAndArticle>> getAllArticle() {
        return RestBean.success("获取成功！",articleService.getAllArticles());
    }
    @GetMapping("getArticleById")
    public RestBean<Article> getArticleById(@RequestParam("id") Integer id,
                                            HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id");
        return RestBean.success("获取成功！",articleService.getArticleById(userId,id));
    }
    @GetMapping("getArticleByUserId")
    public RestBean<List<Article>> getArticleByUserId(@RequestParam("id") Integer id) {
        return RestBean.success("获取成功！",articleService.getArticlesByUserId(id));
    }
    @PostMapping("checkIsLike")
    public RestBean<Integer> checkIsLike(@RequestParam("id") Integer id,
                                         HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int a=articleService.checkLike(userId,id);
        return RestBean.success("获取成功！",a);
    }
    @PostMapping("like")
    public RestBean<String> like(@RequestParam("id") Integer id,
                                 HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=articleService.incrementArticleLikes(id);
        if(status==1&&articleService.addLike(userId,id,new Date())>=1) return RestBean.success("点赞成功！");
        else return RestBean.failure(503,"点赞失败");
    }
    @PostMapping("unlike")
    public RestBean<String> unlikeArticle(@RequestParam("id") Integer id,
                                          HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=articleService.decrementArticleLikes(id);
        if(status==1&&articleService.deleteLike(userId,id)>=1) return RestBean.success("取消点赞成功！");
        else return RestBean.failure(503,"取消点赞失败");
    }
    @PostMapping("checkIsFavorite")
    public RestBean<Integer> checkIsFavorite(@RequestParam("id") Integer id,
                                             HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int a=articleService.checkFavorite(userId,id);
        return RestBean.success("获取成功！",a);

    }
    @PostMapping("favorite")
    public RestBean<String> favoriteArticle(@RequestParam("id") Integer id,
                                            HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=articleService.incrementArticleFavorites(id);
        if(status==1&&articleService.addFavorite(userId,id,new Date())>=1){
            return RestBean.success("收藏成功！");
        }
        else return RestBean.failure(503,"收藏失败");
    }
    @PostMapping("unfavorite")
    public RestBean<String> unfavoriteArticle(@RequestParam("id") Integer id,
                                              HttpServletRequest request){
        int userId=(Integer) request.getAttribute("id");
        int status=articleService.decrementArticleFavorites(id);
        if(status==1&&articleService.deleteFavorite(userId,id)>=1) return RestBean.success("取消收藏成功！");
        else return RestBean.failure(503,"取消收藏失败");
    }
    //更新文章
    @PostMapping("updateArticle")
    public RestBean<Article> updateArticle(@RequestParam("id") Integer id,
                                           @RequestParam("title") String title,
                                           @RequestParam("content") String content,
                                           @RequestParam("tag") String tag,
                                           @RequestParam("category") String category,
                                           @RequestParam("ispublic") Boolean ispublic,
                                           HttpServletRequest request){
        int authorId=(Integer) request.getAttribute("id");//获取当前登录用户的ID
        Article article=articleService.getArticleById(authorId,id);
        if(article==null||!article.getAuthorId().equals(authorId)){
            return RestBean.failure(404,"文章不存在或无权更新");
        }
        int status=articleService.updateArticle(id,authorId,title,content,new Date(),tag,category,ispublic);
        if(status==1){
            Article updatedArticle = articleService.getArticleById(authorId, id);
            return RestBean.success("文章更新成功！", updatedArticle);
        }
        else{
            return RestBean.failure(503,"文章更新失败");
        }
    }
    //删除文章
    @PostMapping("deleteArticle")
    public RestBean<String> deleteArticle(@RequestParam("id") Integer id,
                                          HttpServletRequest request){
        int authorId=(Integer) request.getAttribute("id");
        Article article=articleService.getArticleById(authorId,id);
        if(article==null||!article.getAuthorId().equals(authorId)){
            return RestBean.failure(404,"文章不存在或无权删除");
        }
        int status=articleService.deleteArticle(id,authorId);
        if(status==1) return RestBean.success("文章删除成功！");
        else return RestBean.failure(503,"文章删除失败");
    }
    @GetMapping("/getArticlesByUser")
    public RestBean<List<Article>> getArticlesByUser(HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id");
        List<Article> articles = articleService.getArticlesByUserId(userId);
        return RestBean.success("获取成功", articles);
    }
    //获取用户收藏和点赞的文章
    @GetMapping("/getLikedArticles")
    public RestBean<List<Article>> getLikedArticles(HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id");
        List<Article> articles = articleService.getLikedArticlesByUserId(userId);
        return RestBean.success("获取成功", articles);
    }

    @GetMapping("/getFavoriteArticles")
    public RestBean<List<Article>> getFavoriteArticles(HttpServletRequest request) {
        int userId = (Integer) request.getAttribute("id");
        List<Article> articles = articleService.getFavoriteArticlesByUserId(userId);
        return RestBean.success("获取成功", articles);
    }
    //获取浏览量前五条
    @GetMapping("/getTopArticles")
    public RestBean<List<Article>> getTopArticles(){
        List<Article> articles=articleService.getTopArticles();
        return RestBean.success("获取成功",articles);
    }

    // 文章搜索
    @GetMapping("/search")
    public RestBean<List<Article>> searchArticles(@RequestParam("keyword") String keyword) {
        List<Article> articles = articleService.searchArticlesByTitle(keyword);
        return RestBean.success("搜索成功", articles);
    }
}
